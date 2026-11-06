package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Jumpscare;
import Engine.SoundPlayer;
import GameObject.Bow;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.Rectangle;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.MapTile;
import Level.NPC;
import Level.Player;
import Level.PlayerProgressManager;
import Level.TileType;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

// A pale ghost that floats toward the player, but keeps a short gap instead of overlapping them
// when close enough it lunges at the player, and the player loses a heart if it hits
// it goes down in 1 hit, and being killed by it plays the ghost jumpscare
// also the base for the other enemies (CreepyStickEnemy, WeirdEnemy, CosmicCreeper, NecromancerBoss),
// which swap in their own sprites, health and attack timing
public class Ghost extends NPC {
    protected float speed = 1.4f;
    protected float followDistance = 60f;

    // Maximum distance at which the ghost detects the player.
    protected float detectionRadius = 220f;

    // getting around walls: walk straight at the target when nothing is in the way,
    // otherwise follow a path of tiles found with a breadth first search, worked out again every REPATH_FRAMES
    private static final int REPATH_FRAMES = 20;
    private final ArrayList<Point> path = new ArrayList<>(); // centres of the tiles still to walk through
    private Point pathGoalTile;
    private int repathTimer = 0;
    // frames in a row it hasn't moved, and how long to keep strictly to the tile centres after getting stuck
    private static final int STUCK_FRAMES = 12;
    private static final int STRICT_PATH_FRAMES = 90;
    private int stuckFrames = 0;
    private int strictPathFrames = 0;

    // PaleGhost.png: 40x40 frames, rows 0 idle (2), 1 walk left (2), 2 attack (4)
    private static final int GHOST_FRAME_SIZE = 40;

    // health values
    public static final int DEFAULT_HEALTH = 1;
    protected int maxHealth;
    protected int health;

    // xp given when banished (puzzles give 50)
    public static final int DEFAULT_XP_REWARD = 15;
    protected int xpReward = DEFAULT_XP_REWARD;

    // attack values, subclasses change these to match their attack animation
    protected float attackRange = 80f; // distance to start an attack
    protected float hitRange = 95f; // distance where the lunge still hits
    private static final int ATTACK_COOLDOWN = 90; // frames to wait between attacks
    protected int lungeStart = 20; // frame the lunge starts on
    protected int lungeFrames = 8;
    protected float lungeSpeed = 2f;
    protected int hitFrame = 24; // frame the hit lands on
    protected int attackLength = 44; // total frames of the ATTACK animation
    protected String attackSound = "ghostAttackSound.wav";
    private boolean isAttacking = false;
    private int attackTimer = 0;
    private int attackCooldown = ATTACK_COOLDOWN;

    // hurt values
    private static final int HURT_FRAMES = 20;
    private static final int KNOCKBACK_FRAMES = 8;
    protected float knockbackSpeed = 3f;
    private int hurtTimer = 0;

    // banish values, a boss can take longer to die and stay on the ground
    protected int banishFrames = 45;
    protected float banishRiseSpeed = 0.6f;
    private boolean isBanished = false;
    private int banishTimer = 0;

    // hearts shown above the enemy once it has noticed the player
    private static final int HEART_SIZE = 12;
    private static final int HEART_SPACING = 14;
    private static BufferedImage heartImage;
    private boolean noticedPlayer = false;

    // item this ghost carries and drops when banished (like a relic), null if it isn't carrying one
    private Function<Point, EnhancedMapTile> drop;
    // the first ghost banished each game also drops the bow
    private static boolean bowDropped = false;

    public Ghost(int id, Point location) {
        this(id, location, DEFAULT_HEALTH);
    }

    public Ghost(int id, Point location, int maxHealth) {
        this(id, location, new SpriteSheet(ImageLoader.loadWithAlpha("PaleGhost.png"), GHOST_FRAME_SIZE, GHOST_FRAME_SIZE), maxHealth);
    }

    // for subclasses that use their own sprite sheet
    protected Ghost(int id, Point location, SpriteSheet spriteSheet, int maxHealth) {
        super(id, location.x, location.y, spriteSheet, "STAND_LEFT");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        if (heartImage == null) {
            heartImage = ImageLoader.loadWithAlpha("heartIcon.png");
        }
    }

    // moves the enemy so its collision bounds (not its image) start at x, y
    // the newer enemies' images are much bigger than their bounds, so placing them by their image would put them in walls
    public void setBoundsLocation(float x, float y) {
        setLocation(x - (getBounds().getX() - getX()), y - (getBounds().getY() - getY()));
    }

    // sprite scale, SmallGhost uses a smaller one
    protected float getSpriteScale() {
        return 3;
    }

    // a ghost that's already being banished can't be hit again, so knives and relics pass through it
    @Override
    public boolean isEnemy() {
        return !isBanished;
    }

    public boolean isBanished() {
        return isBanished;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public boolean isWithinDetectionRadius(Player player) {
        if (isBanished || health <= 0 || player == null) {
            return false;
        }

        float dx = (player.getBounds().getX()
                + player.getBounds().getWidth() / 2f)
                - (getBounds().getX()
                + getBounds().getWidth() / 2f);

        float dy = (player.getBounds().getY()
                + player.getBounds().getHeight() / 2f)
                - (getBounds().getY()
                + getBounds().getHeight() / 2f);

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // A nearby ghost may follow even if it is attacking
        // or temporarily stopped close to Dean.
        return distance <= detectionRadius;
    }

    // gives this ghost an item to drop when banished, e.g. ghost.setDrop(Relic.Ear::new)
    public void setDrop(Function<Point, EnhancedMapTile> drop) {
        this.drop = drop;
    }

    // called when a new game starts, so the next ghost banished drops the bow again
    public static void resetDrops() {
        bowDropped = false;
    }

    // used when restarting from a checkpoint: if the player didn't have the bow then, the next ghost banished drops it again
    public static void setBowDropped(boolean dropped) {
        bowDropped = dropped;
    }

    public boolean canFollowThroughDoor(Player player) {
        return !staysInRoom && isWithinDetectionRadius(player);
    }

    // guards (like the great door's guardians) stay in their room instead of following the player through doors
    private boolean staysInRoom = false;

    public void setStaysInRoom(boolean staysInRoom) {
        this.staysInRoom = staysInRoom;
    }

    // e.g. the boss room's guards notice the player from across the room once the boss wakes up
    public void setDetectionRadius(float detectionRadius) {
        this.detectionRadius = detectionRadius;
    }



    @Override
    public void performAction(Player player) {
        if (isBanished) {
            moveY(-banishRiseSpeed);
            banishTimer--;
            if (banishTimer <= 0) {
                onBanishFinished();
                setMapEntityStatus(MapEntityStatus.REMOVED);
            }
            return;
        }

        float dx = (player.getBounds().getX() + player.getBounds().getWidth() / 2f) - (getBounds().getX() + getBounds().getWidth() / 2f);
        float dy = (player.getBounds().getY() + player.getBounds().getHeight() / 2f) - (getBounds().getY() + getBounds().getHeight() / 2f);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // knockback after being hit
        if (hurtTimer > 0) {
            hurtTimer--;
            if (hurtTimer > HURT_FRAMES - KNOCKBACK_FRAMES && distance > 0) {
                moveXHandleCollision(-dx / distance * knockbackSpeed);
                moveYHandleCollision(-dy / distance * knockbackSpeed);
            }
            return;
        }

        if (isAttacking) {
            performAttack(player, dx, dy, distance);
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Stay idle until the player enters the detection radius.
        noticedPlayer = shouldChase(player, distance);
        if (!noticedPlayer) {
            idle(player);
            return;
        }

        // don't attack while the player is reading, in a cutscene, or doing a puzzle
        boolean playerIsBusy = player.isLocked() || map.isBookOpen() || map.getPuzzleUI().isOpen() || map.getTextbox().isActive();
        if (distance <= attackRange && attackCooldown == 0 && !playerIsBusy && !player.isDead()) {
            SoundPlayer.play(attackSound);
            isAttacking = true;
            attackTimer = 0;
            currentAnimationName = dx < 0 ? "ATTACK_LEFT" : "ATTACK_RIGHT";
            return;
        }

        if (distance > followDistance) {
            travelTo(centerX(player), centerY(player));
        } else {
            path.clear();
            currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
        }
    }

    // takes one step toward (x, y), going around walls and solid objects when they're in the way
    // returns false if there's no way there, so the enemy just waits instead of grinding against a wall
    protected boolean travelTo(float targetX, float targetY) {
        float x = centerX();
        float y = centerY();
        float stepX, stepY;
        if (hasClearLine(x, y, targetX, targetY)) {
            path.clear();
            stepX = targetX;
            stepY = targetY;
        } else {
            Point goalTile = map.getTileIndexByPosition(targetX, targetY);
            repathTimer--;
            if (path.isEmpty() || repathTimer <= 0 || pathGoalTile == null
                    || pathGoalTile.x != goalTile.x || pathGoalTile.y != goalTile.y) {
                findPath(x, y, goalTile);
                pathGoalTile = goalTile;
                repathTimer = REPATH_FRAMES;
            }
            // skip waypoints that can already be walked to directly, so it cuts corners like it would on its own
            // (not right after getting stuck, then it sticks to the tile centres until it's clear of the corner)
            while (strictPathFrames == 0 && path.size() > 1 && hasClearLine(x, y, path.get(1).x, path.get(1).y)) {
                path.remove(0);
            }
            if (!path.isEmpty() && Math.abs(path.get(0).x - x) <= speed && Math.abs(path.get(0).y - y) <= speed) {
                path.remove(0);
            }
            if (path.isEmpty()) {
                currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
                return false;
            }
            stepX = path.get(0).x;
            stepY = path.get(0).y;
        }

        // move along the straight line to the next point (the line that was checked to be clear)
        float dx = stepX - x;
        float dy = stepY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0.5f) {
            float step = Math.min(speed, length);
            // walking straight up or down keeps facing the way it already was
            boolean facingRight = Math.abs(dx) > 0.5f ? dx > 0 : currentAnimationName.contains("RIGHT");
            currentAnimationName = facingRight ? "WALK_RIGHT" : "WALK_LEFT";
            float moved = Math.abs(moveXHandleCollision(dx / length * step)) + Math.abs(moveYHandleCollision(dy / length * step));
            if (strictPathFrames > 0) {
                strictPathFrames--;
            }
            // no progress: something it didn't expect is in the way, so plan again and follow the tiles exactly for a bit
            stuckFrames = moved < 0.05f ? stuckFrames + 1 : 0;
            if (stuckFrames >= STUCK_FRAMES) {
                stuckFrames = 0;
                path.clear();
                repathTimer = 0;
                strictPathFrames = STRICT_PATH_FRAMES;
            }
        }
        return true;
    }

    // breadth first search over the room's tiles, from the enemy's tile to the goal tile
    private void findPath(float fromX, float fromY, Point goalTile) {
        path.clear();
        int width = map.getWidth();
        int height = map.getHeight();
        Point start = map.getTileIndexByPosition(fromX, fromY);
        int startX = (int) start.x, startY = (int) start.y;
        int goalX = (int) goalTile.x, goalY = (int) goalTile.y;
        if (startX < 0 || startY < 0 || startX >= width || startY >= height
                || goalX < 0 || goalY < 0 || goalX >= width || goalY >= height) {
            return;
        }
        int[] cameFrom = new int[width * height];
        java.util.Arrays.fill(cameFrom, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int startIndex = startY * width + startX;
        int goalIndex = goalY * width + goalX;
        cameFrom[startIndex] = startIndex;
        queue.add(startIndex);
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == goalIndex) {
                break;
            }
            int cx = current % width, cy = current / width;
            for (int[] step : steps) {
                int nx = cx + step[0], ny = cy + step[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
                    continue;
                }
                int next = ny * width + nx;
                // the goal tile counts as open even if the player is standing on the edge of a wall tile
                if (cameFrom[next] == -1 && (next == goalIndex || isTileOpen(nx, ny))) {
                    cameFrom[next] = current;
                    queue.add(next);
                }
            }
        }
        if (cameFrom[goalIndex] == -1) {
            return; // no way there
        }
        float tileSize = map.getTileset().getScaledSpriteWidth();
        for (int index = goalIndex; index != startIndex; index = cameFrom[index]) {
            path.add(0, new Point((index % width + 0.5f) * tileSize, (index / width + 0.5f) * tileSize));
        }
    }

    // a floor tile with no solid object (puzzle, rock) on it
    protected boolean isTileOpen(int tileX, int tileY) {
        MapTile tile = map.getMapTile(tileX, tileY);
        if (tile == null || tile.getTileType() != TileType.PASSABLE) {
            return false;
        }
        for (EnhancedMapTile object : map.getEnhancedMapTiles()) {
            if (object.getTileType() == TileType.NOT_PASSABLE && object.intersects(tile)) {
                return false;
            }
        }
        return true;
    }

    // true if the enemy's collision box fits with its centre at (x, y)
    protected boolean canStandAt(float x, float y) {
        return hasClearLine(x, y, x, y);
    }

    // true if the enemy's collision box could slide from (x1, y1) to (x2, y2) without touching a wall or solid object
    private boolean hasClearLine(float x1, float y1, float x2, float y2) {
        float halfWidth = getBounds().getWidth() / 2f;
        float halfHeight = getBounds().getHeight() / 2f;
        float length = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        int samples = Math.max(1, (int) (length / 12));
        for (int i = 0; i <= samples; i++) {
            float x = x1 + (x2 - x1) * i / samples;
            float y = y1 + (y2 - y1) * i / samples;
            if (!isPointOpen(x - halfWidth, y - halfHeight) || !isPointOpen(x + halfWidth - 1, y - halfHeight)
                    || !isPointOpen(x - halfWidth, y + halfHeight - 1) || !isPointOpen(x + halfWidth - 1, y + halfHeight - 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPointOpen(float x, float y) {
        MapTile tile = map.getTileByPosition(x, y);
        if (tile == null) {
            return false;
        }
        if (tile.getTileType() == TileType.NOT_PASSABLE && contains(tile.getBounds(), x, y)) {
            return false;
        }
        for (EnhancedMapTile object : map.getEnhancedMapTiles()) {
            if (object.getTileType() == TileType.NOT_PASSABLE && contains(object.getBounds(), x, y)) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(Rectangle bounds, float x, float y) {
        return x >= bounds.getX1() && x < bounds.getX1() + bounds.getWidth() && y >= bounds.getY1() && y < bounds.getY1() + bounds.getHeight();
    }

    protected float centerX() {
        return getBounds().getX() + getBounds().getWidth() / 2f;
    }

    protected float centerY() {
        return getBounds().getY() + getBounds().getHeight() / 2f;
    }

    protected static float centerX(Player player) {
        return player.getBounds().getX() + player.getBounds().getWidth() / 2f;
    }

    protected static float centerY(Player player) {
        return player.getBounds().getY() + player.getBounds().getHeight() / 2f;
    }

    // true while the enemy should go after the player
    protected boolean shouldChase(Player player, float distance) {
        return distance <= detectionRadius;
    }

    // called when an attack takes the player's last heart, each enemy plays its own jumpscare
    protected void onKillPlayer(Player player) {
        Jumpscare.play(Jumpscare.Type.GHOST);
    }

    // called when the enemy is hurt but not banished
    protected void onHurt() { }

    // called when the enemy's health reaches 0, and when its banish animation has finished
    protected void onBanished() { }
    protected void onBanishFinished() { }

    // true while it's attacking, recoiling from a hit, or being banished, so subclasses don't start anything else
    protected boolean isBusy() {
        return isAttacking || hurtTimer > 0 || isBanished;
    }

    // what the enemy does while it isn't chasing the player
    protected void idle(Player player) {
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
    }

    // lunges at the player and hits partway through the animation
    private void performAttack(Player player, float dx, float dy, float distance) {
        attackTimer++;

        if (attackTimer > lungeStart && attackTimer <= lungeStart + lungeFrames && distance > 0) {
            moveXHandleCollision(dx / distance * lungeSpeed);
            moveYHandleCollision(dy / distance * lungeSpeed);
        }

        if (attackTimer == hitFrame && distance <= hitRange && player.takeDamage() && player.isDead()) {
            onKillPlayer(player);
        }

        if (attackTimer >= attackLength) {
            stopAttacking();
        }
    }

    private void stopAttacking() {
        isAttacking = false;
        attackCooldown = ATTACK_COOLDOWN;
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
    }

    // takes damage from the knife or a relic, banished at 0 health
    @Override
    public void hitByDefensiveItem(int damage) {
        if (isBanished) {
            return;
        }
        health = Math.max(0, health - damage);
        if (isAttacking) {
            stopAttacking();
        }
        if (health == 0) {
            PlayerProgressManager.awardXP(xpReward);
            dropItems();
            isBanished = true;
            // the fading ghost no longer blocks the player, so it can't push them into a wall
            setIsUncollidable(true);
            banishTimer = banishFrames;
            currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
            onBanished();
        } else {
            hurtTimer = HURT_FRAMES;
            onHurt();
        }
    }

    // gone without any fuss: no XP, no drop, no fading (e.g. a boss that was already beaten before a checkpoint)
    public void removeFromRoom() {
        health = 0;
        isBanished = true;
        setIsUncollidable(true);
        setMapEntityStatus(MapEntityStatus.REMOVED);
    }

    // fades away without giving XP or dropping anything, e.g. the boss's summoned minions when the boss falls
    public void vanish() {
        if (isBanished) {
            return;
        }
        if (isAttacking) {
            stopAttacking();
        }
        health = 0;
        isBanished = true;
        setIsUncollidable(true);
        banishTimer = banishFrames;
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
        onBanished();
    }

    // leaves the carried item (and the bow, if this is the first ghost banished) where the ghost was
    private void dropItems() {
        // items are 32x32 in the world, so center them on the ghost
        float x = getBounds().getX() + getBounds().getWidth() / 2f - 16;
        float y = getBounds().getY() + getBounds().getHeight() / 2f - 16;
        if (drop != null) {
            map.addEnhancedMapTile(drop.apply(new Point(x, y)));
            drop = null;
            x += 36; // so the bow doesn't land on top of it
        }
        if (!bowDropped) {
            bowDropped = true;
            map.addEnhancedMapTile(new Bow(new Point(x, y)));
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        float scale = getSpriteScale();
        HashMap<String, Frame[]> animations = new HashMap<>();
        // the art faces left, it's flipped for right
        for (boolean flip : new boolean[] {false, true}) {
            String side = flip ? "_RIGHT" : "_LEFT";
            animations.put("STAND" + side, ghostRow(spriteSheet, 0, new int[] {30, 30}, scale, flip));
            animations.put("WALK" + side, ghostRow(spriteSheet, 1, new int[] {12, 12}, scale, flip));
            // rise, eyes turn red, lunge with mouth open, recover; adds up to attackLength
            animations.put("ATTACK" + side, ghostRow(spriteSheet, 2, new int[] {12, 8, 14, 10}, scale, flip));
        }
        return animations;
    }

    private static Frame[] ghostRow(SpriteSheet spriteSheet, int row, int[] delays, float scale, boolean flip) {
        Frame[] frames = new Frame[delays.length];
        for (int i = 0; i < delays.length; i++) {
            FrameBuilder builder = new FrameBuilder(spriteSheet.getSprite(row, i), delays[i])
                    .withScale(scale)
                    .withBounds(13, 22, 14, 15);
            if (flip) {
                builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
            }
            frames[i] = builder.build();
        }
        return frames;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        Composite oldComposite = g.getComposite();

        if (isBanished) {
            // fade out while being banished
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, banishTimer / (float) banishFrames)));
        } else if (hurtTimer > 0 && (hurtTimer / 3) % 2 == 0) {
            // flicker after being hit
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }
        super.draw(graphicsHandler);
        g.setComposite(oldComposite);

        // hearts show once it has noticed the player or been hurt
        if (!isBanished && showsHearts() && (noticedPlayer || health < maxHealth)) {
            drawHearts(graphicsHandler);
        }
    }

    // one heart per health point (lost ones faded), centred over the enemy's bounds
    private void drawHearts(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        Composite oldComposite = g.getComposite();
        int totalWidth = (maxHealth - 1) * HEART_SPACING + HEART_SIZE;
        int x = Math.round(getCalibratedBounds().getX() + getBounds().getWidth() / 2f) - totalWidth / 2;
        int y = Math.round(getCalibratedYLocation()) + getHeartsOffsetY();
        for (int i = 0; i < maxHealth; i++) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, i < health ? 1f : 0.25f));
            graphicsHandler.drawImage(heartImage, x + i * HEART_SPACING, y, HEART_SIZE, HEART_SIZE);
        }
        g.setComposite(oldComposite);
    }

    // false for enemies whose health is shown somewhere else (the boss's bar)
    protected boolean showsHearts() {
        return true;
    }

    // where the hearts go relative to the top of the image, for sprites with empty space above the enemy
    protected int getHeartsOffsetY() {
        // the pale ghost's head starts about a quarter of the way down its frame
        return Math.round(10 * getSpriteScale()) - HEART_SIZE - 4;
    }
}
