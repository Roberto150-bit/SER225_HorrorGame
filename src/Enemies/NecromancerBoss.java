package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Jumpscare;
import Engine.SoundPlayer;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Scripts.ChapterOneEndingScript;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

// The Necromancer, the boss of Chapter 1, waiting on the dais of its ritual chamber behind the great door
// it keeps its distance and throws fire skulls, blasts the player with its staff up close,
// and once it's badly hurt it summons WeirdEnemies to help
// beating it ends Chapter 1 (the story goes on), being killed by it plays the slasher jumpscare
// has 10 health (hearts)
public class NecromancerBoss extends Ghost {
    public static final int BOSS_HEALTH = 10;
    public static final int BOSS_XP_REWARD = 100;

    // NecromancerBoss.png rows: 0 idle (8), 1 walk (8), 2 staff blast (13), 3 summon (13), 4 fire skull cast (17), 5 hurt (5), 6 death (9)
    private static final int FRAME_WIDTH = 160;
    private static final int FRAME_HEIGHT = 128;
    private static final float SCALE = 2.5f;
    // collision box around its feet, in sprite pixels
    private static final int BOUNDS_X = 64;
    private static final int BOUNDS_Y = 93;
    private static final int BOUNDS_WIDTH = 32;
    private static final int BOUNDS_HEIGHT = 22;

    private static final int BLAST_DELAY = 6; // 13 frames, the staff bursts on frame 8
    private static final int SUMMON_DELAY = 6; // 13 frames
    private static final int CAST_DELAY = 4; // 17 frames, the skull flies on frame 10
    private static final int HURT_DELAY = 4; // 5 frames, matches Ghost's hurt time
    private static final int DEATH_DELAY = 7; // 9 frames

    // fire skulls
    private static final int CAST_LENGTH = 17 * CAST_DELAY;
    private static final int CAST_RELEASE_FRAME = 10 * CAST_DELAY;
    private static final int CAST_COOLDOWN = 140;
    private static final int ENRAGED_CAST_COOLDOWN = 85;
    private static final float FIREBALL_SPEED = 3.2f;
    private static final int FIREBALL_LIFE = 240;
    private static final float FIREBALL_HIT_RANGE = 26f;
    private int fireballCooldown = 90;
    private final ArrayList<Fireball> fireballs = new ArrayList<>();

    // summoning, only once it's down to SUMMON_HEALTH or less
    private static final int SUMMON_LENGTH = 13 * SUMMON_DELAY;
    private static final int SUMMON_COOLDOWN = 600;
    private static final int SUMMON_HEALTH = 5;
    private static final int MAX_MINIONS = 2;
    private static final int FIRST_MINION_ID = 900;
    private int summonCooldown = 0;
    private int nextMinionId = FIRST_MINION_ID;
    private final ArrayList<WeirdEnemy> minions = new ArrayList<>();

    // it waits on its dais until the player steps past awakenLineY (or attacks it), then the encounter begins
    private boolean awake = false;
    private float awakenLineY = Float.MAX_VALUE;
    private static final float AWAKEN_DISTANCE = 230f;
    // the intro is played in full the first time, and cut short when the player comes back after dying
    private static boolean introSeenThisGame = false;
    // the servants that stand with it; they notice the player from anywhere in the room once it wakes
    private final ArrayList<Ghost> guards = new ArrayList<>();

    // below half health it's enraged: it moves faster, throws fire skulls more often, and burns with a red aura
    private static final int ENRAGE_HEALTH = BOSS_HEALTH / 2;
    private static final float ENRAGED_SPEED = 1.25f;
    private boolean enraged = false;
    private int auraTimer = 0;

    private enum Cast { NONE, FIRE_SKULL, SUMMON }
    private Cast cast = Cast.NONE;
    private int castTimer = 0;

    private static class Fireball {
        float x, y, vx, vy;
        int life = FIREBALL_LIFE;
    }

    public NecromancerBoss(int id, Point location) {
        super(id, location, new SpriteSheet(ImageLoader.loadWithAlpha("NecromancerBoss.png"), FRAME_WIDTH, FRAME_HEIGHT), BOSS_HEALTH);
        speed = 0.9f;
        // walks in until the player is inside its staff's reach (attackRange), throws fire skulls from further away
        followDistance = 100f;
        detectionRadius = 700f; // the whole room
        xpReward = BOSS_XP_REWARD;
        knockbackSpeed = 1f;
        attackRange = 120f;
        hitRange = 150f;
        lungeFrames = 0; // the staff blast doesn't move it
        hitFrame = 8 * BLAST_DELAY;
        attackLength = 13 * BLAST_DELAY;
        banishFrames = 9 * DEATH_DELAY;
        banishRiseSpeed = 0f; // crumbles where it stands
    }

    // it guards its room instead of chasing the player through doors
    @Override
    public boolean canFollowThroughDoor(Player player) {
        return false;
    }

    // the y position (in the room) the player has to cross to wake it, e.g. the edge of the ritual rug
    public void setAwakenLineY(float awakenLineY) {
        this.awakenLineY = awakenLineY;
    }

    public void addGuard(Ghost guard) {
        guards.add(guard);
    }

    public boolean isAwake() {
        return awake;
    }

    public boolean isEnraged() {
        return enraged;
    }

    // called when a new game starts, so the full intro plays again
    public static void resetIntro() {
        introSeenThisGame = false;
    }

    private void awaken() {
        if (awake) {
            return;
        }
        awake = true;
        // a moment after the intro before the first fire skull
        fireballCooldown = 100;
        for (Ghost guard : guards) {
            guard.setDetectionRadius(detectionRadius);
        }
        map.setActiveScript(new Scripts.BossIntroScript(!introSeenThisGame));
        introSeenThisGame = true;
    }

    // attacking it while it waits wakes it up too
    @Override
    public void hitByDefensiveItem(int damage) {
        awaken();
        super.hitByDefensiveItem(damage);
    }

    @Override
    public void performAction(Player player) {
        auraTimer++;
        if (!awake) {
            // waiting on the dais, facing whoever comes in
            currentAnimationName = centerX(player) < centerX() ? "STAND_LEFT" : "STAND_RIGHT";
            float dx = centerX(player) - centerX();
            float dy = centerY(player) - centerY();
            boolean close = dx * dx + dy * dy <= AWAKEN_DISTANCE * AWAKEN_DISTANCE;
            if (!player.isDead() && (centerY(player) < awakenLineY || close)) {
                awaken();
            }
            return;
        }

        updateFireballs(player);

        boolean waiting = player.isLocked() || map.getTextbox().isActive();
        if (!waiting) {
            if (fireballCooldown > 0) {
                fireballCooldown--;
            }
            if (summonCooldown > 0) {
                summonCooldown--;
            }
        }

        if (!enraged && health > 0 && health <= ENRAGE_HEALTH) {
            enraged = true;
            speed = ENRAGED_SPEED;
            SoundPlayer.play("slasherScream.wav");
        }

        if (cast != Cast.NONE) {
            updateCast(player);
            return;
        }

        float dx = centerX(player) - centerX();
        float dy = centerY(player) - centerY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        boolean playerIsBusy = player.isLocked() || map.isBookOpen() || map.getPuzzleUI().isOpen() || map.getTextbox().isActive();
        if (!isBusy() && !playerIsBusy && !player.isDead() && distance <= detectionRadius) {
            if (health <= SUMMON_HEALTH && summonCooldown == 0 && aliveMinions() < MAX_MINIONS) {
                startCast(Cast.SUMMON, dx);
                return;
            }
            if (fireballCooldown == 0 && distance > attackRange + 30) {
                startCast(Cast.FIRE_SKULL, dx);
                return;
            }
        }

        // walking and the staff blast up close are the normal enemy behaviour
        super.performAction(player);
    }

    private void startCast(Cast type, float dx) {
        cast = type;
        castTimer = 0;
        String side = dx < 0 ? "LEFT" : "RIGHT";
        currentAnimationName = (type == Cast.SUMMON ? "SUMMON_" : "CAST_") + side;
    }

    private void updateCast(Player player) {
        castTimer++;
        if (cast == Cast.FIRE_SKULL) {
            if (castTimer == CAST_RELEASE_FRAME) {
                throwFireball(player);
            }
            if (castTimer >= CAST_LENGTH) {
                endCast();
                fireballCooldown = enraged ? ENRAGED_CAST_COOLDOWN : CAST_COOLDOWN;
            }
        } else if (cast == Cast.SUMMON) {
            if (castTimer >= SUMMON_LENGTH) {
                summonMinion();
                endCast();
                summonCooldown = SUMMON_COOLDOWN;
            }
        }
    }

    private void endCast() {
        cast = Cast.NONE;
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
    }

    // the skull leaves from its raised hand, aimed where the player is now
    private void throwFireball(Player player) {
        Fireball fireball = new Fireball();
        boolean facingRight = currentAnimationName.contains("RIGHT");
        fireball.x = centerX() + (facingRight ? 40 : -40);
        fireball.y = getBounds().getY() - 45;
        float dx = centerX(player) - fireball.x;
        float dy = centerY(player) - fireball.y;
        float length = (float) Math.max(1, Math.sqrt(dx * dx + dy * dy));
        fireball.vx = dx / length * FIREBALL_SPEED;
        fireball.vy = dy / length * FIREBALL_SPEED;
        fireballs.add(fireball);
        SoundPlayer.play(attackSound);
    }

    private void updateFireballs(Player player) {
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fireball = fireballs.get(i);
            fireball.x += fireball.vx;
            fireball.y += fireball.vy;
            fireball.life--;
            MapTile tile = map.getTileByPosition(fireball.x, fireball.y);
            boolean hitWall = tile == null || tile.getTileType() == TileType.NOT_PASSABLE;
            float dx = centerX(player) - fireball.x;
            float dy = centerY(player) - fireball.y;
            boolean hitPlayer = !player.isDead() && dx * dx + dy * dy <= FIREBALL_HIT_RANGE * FIREBALL_HIT_RANGE;
            if (hitPlayer && player.takeDamage() && player.isDead()) {
                onKillPlayer(player);
            }
            if (hitPlayer || hitWall || fireball.life <= 0) {
                fireballs.remove(i);
            }
        }
    }

    // a WeirdEnemy rises next to it, if there's open floor there
    private void summonMinion() {
        float tileSize = map.getTileset().getScaledSpriteWidth();
        float[][] offsets = {{-1.5f, 0}, {1.5f, 0}, {0, 1.5f}, {0, -1.5f}};
        for (float[] offset : offsets) {
            float x = centerX() + offset[0] * tileSize;
            float y = centerY() + offset[1] * tileSize;
            MapTile tile = map.getTileByPosition(x, y);
            if (tile == null || tile.getTileType() != TileType.PASSABLE) {
                continue;
            }
            WeirdEnemy minion = new WeirdEnemy(nextMinionId++, new Point(0, 0));
            minion.setBoundsLocation(x - minion.getBounds().getWidth() / 2f, y - minion.getBounds().getHeight() / 2f);
            map.addNPC(minion);
            minions.add(minion);
            return;
        }
    }

    private int aliveMinions() {
        minions.removeIf(minion -> minion.getMapEntityStatus() == MapEntityStatus.REMOVED || minion.getHealth() <= 0);
        return minions.size();
    }

    // being killed by it shows the slasher
    @Override
    protected void onKillPlayer(Player player) {
        Jumpscare.play(Jumpscare.Type.SLASHER);
    }

    @Override
    protected void onHurt() {
        cast = Cast.NONE;
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "HURT_RIGHT" : "HURT_LEFT";
    }

    @Override
    protected void onBanished() {
        cast = Cast.NONE;
        fireballs.clear();
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "DEATH_RIGHT" : "DEATH_LEFT";
        // the encounter is over: whatever it summoned fades with it (no XP for those)
        for (WeirdEnemy minion : minions) {
            minion.vanish();
        }
        minions.clear();
        // its servants fall with it
        for (Ghost guard : guards) {
            guard.vanish();
        }
    }

    // the player already beat it this playthrough (coming back to its room, or restarting from a checkpoint after)
    public void setAlreadyDefeated() {
        awake = true;
        removeFromRoom();
        for (Ghost guard : guards) {
            guard.removeFromRoom();
        }
    }

    // once it has crumbled, the Chapter 1 ending plays (its last line ends the level)
    // this only happens once: the boss is removed from the map right after
    @Override
    protected void onBanishFinished() {
        map.setActiveScript(new ChapterOneEndingScript());
    }

    // true once its health has hit 0 (while it crumbles and after)
    public boolean isDefeated() {
        return getHealth() <= 0;
    }

    @Override
    protected int getHeartsOffsetY() {
        return 120;
    }

    // its own health bar is shown across the top of the screen, so no hearts over its head
    @Override
    protected boolean showsHearts() {
        return false;
    }

    // fire skulls and the enraged aura glow through the darkness
    @Override
    public void drawAboveDarkness(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        float auraX = getCalibratedBounds().getX() + getBounds().getWidth() / 2f;
        float auraY = getCalibratedBounds().getY() - 70;
        if (!awake) {
            // two embers for eyes in the dark
            float flicker = 0.6f + 0.2f * (float) Math.sin(auraTimer / 15.0);
            EnhancedMapTiles.RitualFire.glow(g, auraX - 6, auraY - 30, 10, new Color(255, 40, 30), flicker);
            EnhancedMapTiles.RitualFire.glow(g, auraX + 6, auraY - 30, 10, new Color(255, 40, 30), flicker);
        } else if (enraged && !isDefeated()) {
            float pulse = 0.45f + 0.2f * (float) Math.sin(auraTimer / 6.0);
            EnhancedMapTiles.RitualFire.glow(g, auraX, auraY, 120, new Color(200, 0, 10), pulse);
        }
        // fire skulls: a dark red glow around a hot core
        for (Fireball fireball : fireballs) {
            int x = Math.round(fireball.x - map.getCamera().getX());
            int y = Math.round(fireball.y - map.getCamera().getY());
            int flicker = (fireball.life / 3) % 2;
            g.setColor(new Color(120, 0, 0, 120));
            g.fillOval(x - 16 - flicker, y - 16 - flicker, 32 + flicker * 2, 32 + flicker * 2);
            g.setColor(new Color(220, 30, 20));
            g.fillOval(x - 10, y - 10, 20, 20);
            g.setColor(new Color(255, 200, 170));
            g.fillOval(x - 4, y - 5, 8, 8);
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> animations = new HashMap<>();
        addBothSides(animations, "STAND", spriteSheet, 0, 8, 8);
        addBothSides(animations, "WALK", spriteSheet, 1, 8, 8);
        addBothSides(animations, "ATTACK", spriteSheet, 2, 13, BLAST_DELAY);
        addBothSides(animations, "SUMMON", spriteSheet, 3, 13, SUMMON_DELAY);
        addBothSides(animations, "CAST", spriteSheet, 4, 17, CAST_DELAY);
        addBothSides(animations, "HURT", spriteSheet, 5, 5, HURT_DELAY);
        addBothSides(animations, "DEATH", spriteSheet, 6, 9, DEATH_DELAY);
        return animations;
    }

    // NAME_LEFT uses the art as drawn, NAME_RIGHT flips it
    private static void addBothSides(HashMap<String, Frame[]> animations, String name, SpriteSheet spriteSheet, int row, int frameCount, int delay) {
        animations.put(name + "_LEFT", row(spriteSheet, row, frameCount, delay, false));
        animations.put(name + "_RIGHT", row(spriteSheet, row, frameCount, delay, true));
    }

    private static Frame[] row(SpriteSheet spriteSheet, int row, int frameCount, int delay, boolean flip) {
        Frame[] frames = new Frame[frameCount];
        for (int i = 0; i < frameCount; i++) {
            FrameBuilder builder = new FrameBuilder(spriteSheet.getSprite(row, i), delay)
                    .withScale(SCALE)
                    .withBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT);
            if (flip) {
                builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
            }
            frames[i] = builder.build();
        }
        return frames;
    }
}
