package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.Jumpscare;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;
import Utils.Point;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.HashMap;

// A trap: a faint stain on the floor that's easy to walk over
// stepping on it makes the creeper rise out of the dark behind the player, and it creeps after them,
// but only while the player has their back to it; when they turn and look, it stops dead
// if it reaches the player it kills them (its jumpscare plays), 1 hit from a defensive item banishes it,
// and if it hasn't caught them after a while it sinks back into the dark until the stain is stepped on again
public class CosmicCreeper extends Ghost {
    public static final int CREEPER_HEALTH = 1;
    public static final int CREEPER_XP_REWARD = 20;

    // CosmicCreeper.png is one 48x144 frame
    private static final int FRAME_WIDTH = 48;
    private static final int FRAME_HEIGHT = 144;
    private static final float SCALE = 1.1f;
    private static final int BOUNDS_X = 12;
    private static final int BOUNDS_Y = 128;
    private static final int BOUNDS_WIDTH = 24;
    private static final int BOUNDS_HEIGHT = 16;

    private static final float TRIGGER_RADIUS = 28f; // how close to the stain's centre the player has to step
    private static final float CREEP_SPEED = 1.15f;
    private static final float CATCH_RANGE = 40f;
    // it counts as being looked at when it's within this much of the way the player is facing (dot product)
    private static final float WATCHED_DOT = 0.35f;
    // where it rises: ideally this many tiles straight behind the player, never closer than the minimum
    private static final float RISE_DISTANCE_IN_TILES = 3.5f;
    private static final float MIN_RISE_DISTANCE_IN_TILES = 2f;
    private static final int MAX_RISE_STEPS = 8; // how far across the floor it looks for a spot

    private static final int RISE_FRAMES = 45;
    private static final int HUNT_FRAMES = 25 * 60; // gives up after 25 seconds
    private static final int SINK_FRAMES = 45;
    private static final int REARM_FRAMES = 30 * 60; // the stain works again 30 seconds later

    private enum State { DORMANT, RISING, CREEPING, SINKING, RESTING }
    private State state = State.DORMANT;
    private int stateTimer = 0;
    private boolean beingWatched = false;

    // where the stain is (centre of the trap tile)
    private float trapX;
    private float trapY;
    private boolean trapSet = false;

    public CosmicCreeper(int id, Point location) {
        super(id, location, new SpriteSheet(ImageLoader.loadWithAlpha("CosmicCreeper.png"), FRAME_WIDTH, FRAME_HEIGHT), CREEPER_HEALTH);
        speed = CREEP_SPEED;
        xpReward = CREEPER_XP_REWARD;
        detectionRadius = 400f;
        // it never blocks the player, it's something that's only half there
        setIsUncollidable(true);
    }

    // only counts as hunting (for the chase music) while it's out of the dark
    @Override
    public boolean isWithinDetectionRadius(Player player) {
        return isVisible() && super.isWithinDetectionRadius(player);
    }

    @Override
    public boolean canFollowThroughDoor(Player player) {
        return false;
    }

    // only a target while it's out of the dark, so a thrown relic isn't used up on the hidden stain
    @Override
    public boolean isEnemy() {
        return isVisible() && super.isEnemy();
    }

    // can only be hit while it's out of the dark
    @Override
    public void hitByDefensiveItem(int damage) {
        if (isVisible()) {
            super.hitByDefensiveItem(damage);
        }
    }

    private boolean isVisible() {
        return state == State.RISING || state == State.CREEPING || state == State.SINKING;
    }

    @Override
    public void performAction(Player player) {
        if (!trapSet) {
            trapX = centerX();
            trapY = centerY();
            trapSet = true;
        }
        if (isBusy()) {
            // being banished
            super.performAction(player);
            return;
        }
        stateTimer++;
        boolean playerIsBusy = player.isLocked() || map.isBookOpen() || map.getPuzzleUI().isOpen() || map.getTextbox().isActive();

        switch (state) {
            case DORMANT:
                float tx = centerX(player) - trapX;
                float ty = centerY(player) - trapY;
                if (!player.isDead() && tx * tx + ty * ty <= TRIGGER_RADIUS * TRIGGER_RADIUS && riseBehind(player)) {
                    setState(State.RISING);
                }
                break;
            case RISING:
                faceTowards(player);
                if (stateTimer >= RISE_FRAMES) {
                    setState(State.CREEPING);
                }
                break;
            case CREEPING:
                if (stateTimer >= HUNT_FRAMES || player.isDead()) {
                    setState(State.SINKING);
                    break;
                }
                float dx = centerX(player) - centerX();
                float dy = centerY(player) - centerY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance <= CATCH_RANGE && !playerIsBusy) {
                    player.kill();
                    onKillPlayer(player);
                    setState(State.SINKING);
                    break;
                }
                beingWatched = isWatchedBy(player, dx, dy, distance);
                if (beingWatched || playerIsBusy) {
                    faceTowards(player);
                } else {
                    travelTo(centerX(player), centerY(player));
                }
                break;
            case SINKING:
                if (stateTimer >= SINK_FRAMES) {
                    // back under the stain, waiting
                    setBoundsLocation(trapX - getBounds().getWidth() / 2f, trapY - getBounds().getHeight() / 2f);
                    setState(State.RESTING);
                }
                break;
            case RESTING:
                if (stateTimer >= REARM_FRAMES) {
                    setState(State.DORMANT);
                }
                break;
        }
    }

    private void setState(State newState) {
        state = newState;
        stateTimer = 0;
    }

    // the player is looking at it if it's roughly in the direction they're facing
    private static boolean isWatchedBy(Player player, float dx, float dy, float distance) {
        if (distance < 1) {
            return true;
        }
        Point aim = player.getAimDirection();
        // dx, dy point from the creeper to the player, so flip them to get player -> creeper
        float dot = (-dx * aim.x - dy * aim.y) / distance;
        return dot > WATCHED_DOT;
    }

    // appears behind the player, on floor that's connected to where they're standing
    // (so never past a wall, in a wall, or in the dark space around a room)
    private boolean riseBehind(Player player) {
        Point aim = player.getAimDirection();
        float tileSize = map.getTileset().getScaledSpriteWidth();
        Point start = map.getTileIndexByPosition(centerX(player), centerY(player));
        int width = map.getWidth();
        int height = map.getHeight();
        int startX = (int) start.x, startY = (int) start.y;
        if (startX < 0 || startY < 0 || startX >= width || startY >= height) {
            return false;
        }

        // walk out from the player's tile across open floor, scoring each tile by how far behind them it is
        boolean[] seen = new boolean[width * height];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startX, startY, 0});
        seen[startY * width + startX] = true;
        float bestScore = Float.NEGATIVE_INFINITY;
        int bestX = -1, bestY = -1;
        while (!queue.isEmpty()) {
            int[] tile = queue.poll();
            float dx = tile[0] - startX;
            float dy = tile[1] - startY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance >= MIN_RISE_DISTANCE_IN_TILES && fitsOnFloor(tile[0], tile[1], tileSize)) {
                // straight behind the player is best, and about RISE_DISTANCE_IN_TILES away
                float behind = -(dx * aim.x + dy * aim.y) / distance;
                float score = behind - Math.abs(distance - RISE_DISTANCE_IN_TILES) * 0.3f;
                if (score > bestScore) {
                    bestScore = score;
                    bestX = tile[0];
                    bestY = tile[1];
                }
            }
            if (tile[2] >= MAX_RISE_STEPS) {
                continue;
            }
            for (int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = tile[0] + step[0], ny = tile[1] + step[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height || seen[ny * width + nx] || !isTileOpen(nx, ny)) {
                    continue;
                }
                seen[ny * width + nx] = true;
                queue.add(new int[] {nx, ny, tile[2] + 1});
            }
        }
        if (bestX < 0) {
            return false; // nowhere to rise, it tries again while the player is still on the stain
        }
        float x = (bestX + 0.5f) * tileSize;
        float y = (bestY + 0.5f) * tileSize;
        setBoundsLocation(x - getBounds().getWidth() / 2f, y - getBounds().getHeight() / 2f);
        faceTowards(player);
        return true;
    }

    // true if the creeper's feet, centred on this tile, are entirely on floor tiles (not the walkable edge of a wall)
    private boolean fitsOnFloor(int tileX, int tileY, float tileSize) {
        float x = (tileX + 0.5f) * tileSize;
        float y = (tileY + 0.5f) * tileSize;
        float halfWidth = getBounds().getWidth() / 2f;
        float halfHeight = getBounds().getHeight() / 2f;
        float[][] corners = {{x - halfWidth, y - halfHeight}, {x + halfWidth - 1, y - halfHeight}, {x - halfWidth, y + halfHeight - 1}, {x + halfWidth - 1, y + halfHeight - 1}};
        for (float[] corner : corners) {
            Point tile = map.getTileIndexByPosition(corner[0], corner[1]);
            if (tile.x < 0 || tile.y < 0 || tile.x >= map.getWidth() || tile.y >= map.getHeight() || !isTileOpen((int) tile.x, (int) tile.y)) {
                return false;
            }
        }
        return canStandAt(x, y);
    }

    private void faceTowards(Player player) {
        currentAnimationName = centerX(player) < centerX() ? "STAND_LEFT" : "STAND_RIGHT";
    }

    @Override
    protected void onKillPlayer(Player player) {
        Jumpscare.play(Jumpscare.Type.CREEPER);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        if (!isVisible() && !isBusy()) {
            // the stain on the floor
            if (state == State.DORMANT && trapSet) {
                int x = Math.round(trapX - map.getCamera().getX());
                int y = Math.round(trapY - map.getCamera().getY());
                g.setColor(new Color(10, 5, 15, 70));
                g.fillOval(x - 26, y - 12, 52, 24);
                g.setColor(new Color(10, 5, 15, 50));
                g.fillOval(x - 16, y - 7, 32, 14);
            }
            return;
        }
        float alpha = 1f;
        if (state == State.RISING) {
            alpha = Math.min(1f, stateTimer / (float) RISE_FRAMES);
        } else if (state == State.SINKING) {
            alpha = Math.max(0f, 1f - stateTimer / (float) SINK_FRAMES);
        } else if (state == State.CREEPING && !beingWatched) {
            // flickers a little while it moves
            alpha = (stateTimer / 4) % 3 == 0 ? 0.8f : 1f;
        }
        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.draw(graphicsHandler);
        g.setComposite(oldComposite);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        HashMap<String, Frame[]> animations = new HashMap<>();
        for (boolean flip : new boolean[] {false, true}) {
            String side = flip ? "_RIGHT" : "_LEFT";
            FrameBuilder builder = new FrameBuilder(spriteSheet.getSprite(0, 0))
                    .withScale(SCALE)
                    .withBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT);
            if (flip) {
                builder.withImageEffect(ImageEffect.FLIP_HORIZONTAL);
            }
            Frame[] frame = new Frame[] {builder.build()};
            animations.put("STAND" + side, frame);
            animations.put("WALK" + side, frame);
            animations.put("ATTACK" + side, frame);
        }
        return animations;
    }
}
