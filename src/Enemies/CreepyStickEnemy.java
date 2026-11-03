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

// A tall stick creature that crawls along the floor toward the player in jerky bursts,
// then unfolds to full height, looms over them and bends down to strike
// a hit that kills the player plays the tall figure jumpscare
// has 3 health (hearts)
public class CreepyStickEnemy extends Ghost {
    public static final int STICK_HEALTH = 3;

    // CreepyStickEnemy.png rows: 0 idle (2 frames), 1 crawl (4), 2 loom attack (6)
    private static final int FRAME_WIDTH = 140;
    private static final int FRAME_HEIGHT = 170;
    // collision box around its feet, the rest of the image is the body towering above
    private static final int BOUNDS_X = 52;
    private static final int BOUNDS_Y = 135;
    private static final int BOUNDS_WIDTH = 36;
    private static final int BOUNDS_HEIGHT = 30;
    // crouch, rise, full height, tremble, bend down (the hit), recover; adds up to attackLength
    private static final int[] LOOM_DELAYS = {10, 10, 14, 12, 10, 14};

    // crawls in bursts: fast for a bit, then nearly still
    private static final float CRAWL_SPEED = 1.0f;
    private static final int CRAWL_CYCLE = 60;
    private static final int CRAWL_BURST = 40;
    private int crawlTimer = 0;

    public CreepyStickEnemy(int id, Point location) {
        super(id, location, new SpriteSheet(ImageLoader.loadWithAlpha("CreepyStickEnemy.png"), FRAME_WIDTH, FRAME_HEIGHT), STICK_HEALTH);
        followDistance = 50f;
        detectionRadius = 260f;
        attackRange = 90f;
        hitRange = 110f;
        lungeStart = 44; // leans in just before it bends down
        lungeFrames = 6;
        lungeSpeed = 2.5f;
        hitFrame = 48;
        attackLength = 70;
    }

    @Override
    public void performAction(Player player) {
        crawlTimer = (crawlTimer + 1) % CRAWL_CYCLE;
        speed = crawlTimer < CRAWL_BURST ? CRAWL_SPEED * 1.6f : CRAWL_SPEED * 0.25f;
        super.performAction(player);
    }

    // being killed by it shows the tall figure in the dark
    @Override
    protected void onKillPlayer(Player player) {
        Jumpscare.play(Jumpscare.Type.TALL_FIGURE);
    }

    // the image has a lot of empty space above the creature's head
    @Override
    protected int getHeartsOffsetY() {
        return 40;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_LEFT", row(spriteSheet, 0, new int[] {30, 30}, false));
            put("STAND_RIGHT", row(spriteSheet, 0, new int[] {30, 30}, true));
            put("WALK_LEFT", row(spriteSheet, 1, new int[] {8, 8, 8, 8}, false));
            put("WALK_RIGHT", row(spriteSheet, 1, new int[] {8, 8, 8, 8}, true));
            put("ATTACK_LEFT", row(spriteSheet, 2, LOOM_DELAYS, false));
            put("ATTACK_RIGHT", row(spriteSheet, 2, LOOM_DELAYS, true));
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
