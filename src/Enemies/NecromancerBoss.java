package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Jumpscare;
import Engine.SoundPlayer;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.GameListener;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

// The first level's final boss, waiting in the secret room
// it keeps its distance and throws fire skulls, blasts the player with its staff up close,
// and once it's badly hurt it summons WeirdEnemies to help
// beating it wins the level, being killed by it plays the slasher jumpscare
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

    @Override
    public void performAction(Player player) {
        updateFireballs(player);

        if (fireballCooldown > 0) {
            fireballCooldown--;
        }
        if (summonCooldown > 0) {
            summonCooldown--;
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
                fireballCooldown = CAST_COOLDOWN;
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
    }

    // beating the boss wins the level
    @Override
    protected void onBanishFinished() {
        for (GameListener listener : map.getListeners()) {
            listener.onWin();
        }
    }

    @Override
    protected int getHeartsOffsetY() {
        return 120;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
        // fire skulls: a dark red glow around a hot core
        Graphics2D g = graphicsHandler.getGraphics();
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
