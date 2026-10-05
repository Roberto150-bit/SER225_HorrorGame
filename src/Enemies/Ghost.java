package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapTile;
import Level.NPC;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

// A ghost enemy that wanders the map on its own, and chases the player once they get close
// movement follows a tile-by-tile path found with a breadth-first search, so the ghost goes around walls instead of getting stuck on them
public class Ghost extends NPC {
    private float chaseSpeed = 1.4f;
    private float wanderSpeed = 0.7f;
    private float followDistance = 60f;      // ghost stops when this close to the player (pixels)
    private int detectionRange = 6;          // ghost starts chasing when the player is this many tiles away or closer
    private int wanderRange = 5;             // how many tiles away a random wander destination can be

    private static final int REPATH_FRAMES = 30;   // how often the chase path is recalculated
    private static final int STUCK_FRAMES = 20;    // frames without progress before the ghost gives up on its current path

    private ArrayDeque<Integer> path = new ArrayDeque<>(); // tile indices (x + y * mapWidth) to walk through, in order
    private int repathTimer = 0;
    private int lastGoalTile = -1;
    private boolean isStopped = false;
    private int stuckTimer = 0;
    private int idleTimer = 0;
    private boolean wasChasing = false;
    private final Random random = new Random();

    public Ghost(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Ghost.png"), 16, 22), "STAND_LEFT");
    }

    @Override
    public void performAction(Player player) {
        float dx = centerX(player) - centerX(this);
        float dy = centerY(player) - centerY(this);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // stop when close to the player, but don't start moving again until they've moved a bit further away
        // (without this gap the ghost flickers between stopping and moving right at the edge of followDistance)
        if (distance <= followDistance || (isStopped && distance <= followDistance + 20)) {
            isStopped = true;
            path.clear();
            stand();
            return;
        }
        isStopped = false;

        // same idea for chasing -- once chasing, the player has to get 2 tiles further away before the ghost gives up
        int tileSize = map.getTileset().getScaledSpriteWidth();
        int range = wasChasing ? detectionRange + 2 : detectionRange;
        boolean chasing = distance <= range * tileSize;

        // switching between wandering and chasing throws away the old path
        if (chasing != wasChasing) {
            path.clear();
            repathTimer = 0;
            lastGoalTile = -1;
            wasChasing = chasing;
        }

        if (chasing) {
            repathTimer--;
            int goalTile = tileIndexOf(centerX(player), centerY(player));
            // only recalculate when the player has moved to a different tile (or the ghost has run out of path)
            if (path.isEmpty() || (repathTimer <= 0 && goalTile != lastGoalTile)) {
                path = findPathFromCurrentPosition(goalTile);
                lastGoalTile = goalTile;
                repathTimer = REPATH_FRAMES;
            }
            if (path.isEmpty()) {
                // player is somewhere the path search can't reach (e.g. standing half on a wall tile), so float straight at them
                moveToward(centerX(player), centerY(player), chaseSpeed);
            } else {
                followPath(chaseSpeed);
            }
        } else {
            if (path.isEmpty()) {
                if (idleTimer > 0) {
                    idleTimer--;
                    stand();
                    return;
                }
                path = findPathFromCurrentPosition(pickWanderTarget());
                if (path.isEmpty()) {
                    idleTimer = 30;
                    return;
                }
            }
            followPath(wanderSpeed);
            if (path.isEmpty()) {
                // reached the destination -- linger for a moment before drifting somewhere else
                idleTimer = 30 + random.nextInt(90);
            }
        }
    }

    // finds a path to the goal starting from wherever the ghost is heading right now
    // if it's already on its way to a tile, the new path continues from that tile, so recalculating never makes the ghost turn back
    private ArrayDeque<Integer> findPathFromCurrentPosition(int goal) {
        if (!path.isEmpty()) {
            int headingTo = path.peek();
            ArrayDeque<Integer> newPath = findPath(headingTo, goal);
            newPath.addFirst(headingTo);
            if (headingTo == goal) {
                newPath.clear();
                newPath.add(goal);
            }
            return newPath;
        }
        return findPath(tileIndexOf(centerX(this), centerY(this)), goal);
    }

    // moves toward the center of the next tile in the path, removing it once reached
    private void followPath(float speed) {
        int next = path.peek();
        int tileSize = map.getTileset().getScaledSpriteWidth();
        float targetX = (next % map.getWidth()) * tileSize + tileSize / 2f;
        float targetY = (next / map.getWidth()) * tileSize + tileSize / 2f;

        moveToward(targetX, targetY, speed);

        if (Math.abs(targetX - centerX(this)) <= speed && Math.abs(targetY - centerY(this)) <= speed) {
            path.poll();
        }
    }

    // moves up to "speed" pixels on each axis toward a point, and tracks whether the ghost is actually making progress
    private void moveToward(float targetX, float targetY, float speed) {
        float dx = targetX - centerX(this);
        float dy = targetY - centerY(this);
        float movedX = moveXHandleCollision(clamp(dx, speed));
        float movedY = moveYHandleCollision(clamp(dy, speed));

        // only turn around for real sideways movement, not tiny alignment nudges
        if (dx < -speed) {
            currentAnimationName = "WALK_LEFT";
        } else if (dx > speed) {
            currentAnimationName = "WALK_RIGHT";
        }

        // if something (another NPC, a wall corner) is blocking the ghost, drop the path so a new one gets picked
        boolean triedToMove = Math.abs(dx) > 0.5f || Math.abs(dy) > 0.5f;
        if (triedToMove && Math.abs(movedX) + Math.abs(movedY) < 0.1f) {
            stuckTimer++;
            if (stuckTimer >= STUCK_FRAMES) {
                stuckTimer = 0;
                path.clear();
                repathTimer = 0;
                idleTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }
    }

    // breadth-first search over the map's tiles (4 directions) from one tile to another
    // returns the tiles to walk through, not including the start tile -- the ghost heads straight for the next tile
    // and slides along walls (via collision handling) if it's slightly off-center, rather than backing up to re-center first
    private ArrayDeque<Integer> findPath(int start, int goal) {
        ArrayDeque<Integer> result = new ArrayDeque<>();
        if (start < 0 || goal < 0) {
            return result;
        }
        HashSet<Integer> blocked = getBlockedTiles();
        if (blocked.contains(goal)) {
            return result;
        }

        int width = map.getWidth();
        int height = map.getHeight();
        int[] cameFrom = new int[width * height];
        java.util.Arrays.fill(cameFrom, -1);
        cameFrom[start] = start;

        ArrayDeque<Integer> frontier = new ArrayDeque<>();
        frontier.add(start);
        int[][] directions = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

        while (!frontier.isEmpty()) {
            int current = frontier.poll();
            if (current == goal) {
                break;
            }
            int cx = current % width;
            int cy = current / width;
            for (int[] direction : directions) {
                int nx = cx + direction[0];
                int ny = cy + direction[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
                    continue;
                }
                int neighbor = nx + ny * width;
                if (cameFrom[neighbor] == -1 && !blocked.contains(neighbor)) {
                    cameFrom[neighbor] = current;
                    frontier.add(neighbor);
                }
            }
        }

        if (cameFrom[goal] == -1) {
            return result;
        }
        for (int tile = goal; tile != start; tile = cameFrom[tile]) {
            result.addFirst(tile);
        }
        return result;
    }

    // tiles the ghost can't move through: non-passable map tiles, plus any solid enhanced map tiles (rocks, doors, etc.)
    private HashSet<Integer> getBlockedTiles() {
        HashSet<Integer> blocked = new HashSet<>();
        int width = map.getWidth();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                MapTile tile = map.getMapTile(x, y);
                if (tile == null || tile.getTileType() == TileType.NOT_PASSABLE) {
                    blocked.add(x + y * width);
                }
            }
        }
        ArrayList<EnhancedMapTile> enhancedMapTiles = map.getEnhancedMapTiles();
        for (EnhancedMapTile enhancedMapTile : enhancedMapTiles) {
            if (enhancedMapTile.getTileType() == TileType.NOT_PASSABLE && !enhancedMapTile.isUncollidable()) {
                int index = tileIndexOf(enhancedMapTile.getBounds().getX() + 1, enhancedMapTile.getBounds().getY() + 1);
                if (index >= 0) {
                    blocked.add(index);
                }
            }
        }
        return blocked;
    }

    // picks a random open tile within wanderRange of the ghost
    private int pickWanderTarget() {
        Point currentTile = map.getTileIndexByPosition(centerX(this), centerY(this));
        HashSet<Integer> blocked = getBlockedTiles();
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = Math.round(currentTile.x) + random.nextInt(wanderRange * 2 + 1) - wanderRange;
            int y = Math.round(currentTile.y) + random.nextInt(wanderRange * 2 + 1) - wanderRange;
            if (x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight() && !blocked.contains(x + y * map.getWidth())) {
                return x + y * map.getWidth();
            }
        }
        return -1;
    }

    // converts a pixel position into a tile index (x + y * mapWidth), or -1 if it is off the map
    private int tileIndexOf(float x, float y) {
        Point tile = map.getTileIndexByPosition(x, y);
        int tx = Math.round(tile.x);
        int ty = Math.round(tile.y);
        if (tx < 0 || ty < 0 || tx >= map.getWidth() || ty >= map.getHeight()) {
            return -1;
        }
        return tx + ty * map.getWidth();
    }

    private void stand() {
        currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
    }

    private static float clamp(float value, float max) {
        return Math.max(-max, Math.min(max, value));
    }

    private static float centerX(GameObject.GameObject gameObject) {
        return gameObject.getBounds().getX() + gameObject.getBounds().getWidth() / 2f;
    }

    private static float centerY(GameObject.GameObject gameObject) {
        return gameObject.getBounds().getY() + gameObject.getBounds().getHeight() / 2f;
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        // hitbox is a bit smaller than one tile so the ghost can fit through one-tile-wide gaps
        HashMap<String, Frame[]> animations = new HashMap<>();

        animations.put("STAND_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(2, 2, 12, 14)
                        .build()
        });
        animations.put("STAND_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(2, 2, 12, 14)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .build()
        });
        animations.put("WALK_LEFT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(2, 2, 12, 14)
                        .build()
        });
        animations.put("WALK_RIGHT", new Frame[] {
                new FrameBuilder(spriteSheet.getSprite(0, 0))
                        .withScale(3)
                        .withBounds(2, 2, 12, 14)
                        .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                        .build()
        });

        return animations;
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
