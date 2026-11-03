package Enemies;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import Engine.Jumpscare;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import Utils.Point;

import java.util.HashMap;

// An antlered figure that lurks against the walls and by the entrances of a room
// it stays near its spot, only goes for the player when they come close, and screams as it lunges
// a hit that kills the player plays the Specimen 11 jumpscare
// it won't follow the player through doors, has 1 health (heart)
public class WeirdEnemy extends Ghost {
    public static final int WEIRD_HEALTH = 1;
    public static final int WEIRD_XP_REWARD = 5;

    // WeirdEnemy.png rows: 0 idle (4 frames), 1 scream attack (6)
    private static final int FRAME_WIDTH = 100;
    private static final int FRAME_HEIGHT = 132;
    private static final int BOUNDS_X = 38;
    private static final int BOUNDS_Y = 55;
    private static final int BOUNDS_WIDTH = 24;
    private static final int BOUNDS_HEIGHT = 40;
    // still, crouch, rear up, scream lunge, scream shake, recover; adds up to attackLength
    private static final int[] SCREAM_DELAYS = {6, 8, 8, 10, 10, 10};

    // how far it will leave its spot before going back
    private static final float LEASH_DISTANCE = 120f;
    private float homeX;
    private float homeY;
    private boolean homeSet = false;

    public WeirdEnemy(int id, Point location) {
        super(id, location, new SpriteSheet(ImageLoader.loadWithAlpha("WeirdEnemy.png"), FRAME_WIDTH, FRAME_HEIGHT), WEIRD_HEALTH);
        speed = 1.6f;
        followDistance = 40f;
        detectionRadius = 150f;
        xpReward = WEIRD_XP_REWARD;
        attackRange = 90f;
        hitRange = 100f;
        lungeStart = 22; // the scream frame
        lungeFrames = 8;
        lungeSpeed = 3f;
        hitFrame = 26;
        attackLength = 52;
    }

    // being killed by it shows Specimen 11
    @Override
    protected void onKillPlayer(Player player) {
        Jumpscare.play(Jumpscare.Type.SPECIMEN_11);
    }

    @Override
    public boolean canFollowThroughDoor(Player player) {
        return false;
    }

    @Override
    public void performAction(Player player) {
        // its spot is wherever the room placed it
        if (!homeSet) {
            homeX = getBounds().getX();
            homeY = getBounds().getY();
            homeSet = true;
        }
        super.performAction(player);
    }

    // only chases while it's still close to its spot
    @Override
    protected boolean shouldChase(Player player, float distance) {
        return super.shouldChase(player, distance) && distanceFromHome() <= LEASH_DISTANCE;
    }

    // drifts back to its spot and waits there
    @Override
    protected void idle(Player player) {
        float dx = homeX - getBounds().getX();
        float dy = homeY - getBounds().getY();
        if (Math.abs(dx) <= speed && Math.abs(dy) <= speed) {
            super.idle(player);
            return;
        }
        currentAnimationName = dx < 0 ? "WALK_LEFT" : "WALK_RIGHT";
        if (Math.abs(dx) > speed) {
            moveXHandleCollision(Math.signum(dx) * speed);
        }
        if (Math.abs(dy) > speed) {
            moveYHandleCollision(Math.signum(dy) * speed);
        }
    }

    private float distanceFromHome() {
        float dx = homeX - getBounds().getX();
        float dy = homeY - getBounds().getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    protected int getHeartsOffsetY() {
        return 4;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_LEFT", row(spriteSheet, 0, new int[] {14, 14, 14, 14}, false));
            put("STAND_RIGHT", row(spriteSheet, 0, new int[] {14, 14, 14, 14}, true));
            put("WALK_LEFT", row(spriteSheet, 0, new int[] {8, 8, 8, 8}, false));
            put("WALK_RIGHT", row(spriteSheet, 0, new int[] {8, 8, 8, 8}, true));
            put("ATTACK_LEFT", row(spriteSheet, 1, SCREAM_DELAYS, false));
            put("ATTACK_RIGHT", row(spriteSheet, 1, SCREAM_DELAYS, true));
        }};
    }

    // one animation from a row of the sheet, the art faces left so it's flipped for right
    private static Frame[] row(SpriteSheet spriteSheet, int row, int[] delays, boolean flip) {
        Frame[] frames = new Frame[delays.length];
        for (int i = 0; i < delays.length; i++) {
            FrameBuilder builder = new FrameBuilder(spriteSheet.getSprite(row, i), delays[i])
                    .withBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT);
            if (flip) {
                builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
            }
            frames[i] = builder.build();
        }
        return frames;
    }
}
