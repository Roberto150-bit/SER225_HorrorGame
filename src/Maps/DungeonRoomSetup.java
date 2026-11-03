package Maps;

import Enemies.CreepyStickEnemy;
import Enemies.Ghost;
import Enemies.WeirdEnemy;
import EnhancedMapTiles.NoteObject;
import EnhancedMapTiles.PuzzleObject;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Level.TileType;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Scripts.CodeNoteScript;
import Scripts.NoteScript;
import Utils.Point;
import java.util.ArrayList;
import java.util.Random;

// shared helpers the dungeon rooms use to place their puzzles, code notes and ghosts
public class DungeonRoomSetup {

    // ids of the puzzles that unlock each door, in the order the player solves them
    public static final String BASEMENT_PUZZLE = "basement_fuse_01";
    public static final String CELL_PUZZLE = "cell_radio_01";
    public static final String LIBRARY_PUZZLE = "library_clock_01";
    public static final String BASIC_ROOM_PUZZLE = "basic_ritual_01";
    public static final String WATER_ROOM_PUZZLE = "water_mirror_01";
    public static final String FINAL_SEAL_PUZZLE = "rundown_final_seal";

    // ghosts are kept at least this many tiles away from any door so they don't catch the player the moment they walk in
    private static final int GHOST_DOOR_DISTANCE = 3;
    // weird enemies lurk closer: right beside an entrance, but not in the doorway itself
    private static final int LURKER_DOOR_DISTANCE = 2;
    private static final int ENTRANCE_DISTANCE = 3;
    private static final int FIRST_GHOST_ID = 100;

    private static final Random random = new Random();

    // a puzzle the player interacts with (E) on the given tile, drawn with that puzzle type's sprite
    public static PuzzleObject puzzle(Map map, int tileX, int tileY, String instanceId, PuzzleId type, PuzzleDifficulty difficulty) {
            if (type == PuzzleId.TUNE_RADIO
                    || type == PuzzleId.SPIRIT_MIRROR
                    || type == PuzzleId.RITUAL_SEQUENCE
                    || type == PuzzleId.HAUNTED_CLOCK) {

                difficulty = PuzzleDifficulty.HARD;
            }

        return new PuzzleObject(map.getMapTile(tileX, tileY).getLocation(), new PuzzleConfig(instanceId, type, difficulty));
    }

    // a story note holding one page (1-5) of the journal
    public static NoteObject storyNote(Map map, int tileX, int tileY, int page) {
        NoteObject note = new NoteObject(map.getMapTile(tileX, tileY).getLocation());
        note.setIsUncollidable(true);
        note.setInteractScript(new NoteScript(page));
        return note;
    }

    // a note holding one digit (0 = first) of the final seal's code
    public static NoteObject codeNote(Map map, int tileX, int tileY, int digitIndex) {
        NoteObject note = new NoteObject(map.getMapTile(tileX, tileY).getLocation());
        note.setIsUncollidable(true);
        note.setInteractScript(new CodeNoteScript(digitIndex));
        return note;
    }

    // places the enemies, a new layout every time the room is built (each playthrough)
    // creepy stick enemies go on random open floor away from the doors, weird enemies lurk against the walls,
    // either beside an entrance or along the sides of the room
    // call at the end of a map's constructor, after its doors and objects are set up, so they can be avoided
    // returns the enemies it placed (stick enemies first), so a room can give one of them an item to drop
    public static ArrayList<Ghost> addRandomGhosts(Map map, int stickCount, int weirdCount) {
        ArrayList<Ghost> enemies = new ArrayList<>();
        ArrayList<Point> doors = findDoors(map);
        ArrayList<Point> openTiles = findGhostSpawnTiles(map, doors, GHOST_DOOR_DISTANCE);
        // small rooms might not have enough space that far from the doors, so allow closer spots if needed
        if (openTiles.size() < stickCount + weirdCount) {
            openTiles = findGhostSpawnTiles(map, doors, GHOST_DOOR_DISTANCE - 1);
        }
        // lurking spots: open tiles against a wall, near an entrance or anywhere along the sides
        ArrayList<Point> entranceTiles = new ArrayList<>();
        ArrayList<Point> sideTiles = new ArrayList<>();
        for (Point tile : findGhostSpawnTiles(map, doors, LURKER_DOOR_DISTANCE)) {
            if (!isAgainstWall(map, (int) tile.x, (int) tile.y)) {
                continue;
            }
            if (distanceToNearestDoor(doors, tile) <= ENTRANCE_DISTANCE) {
                entranceTiles.add(tile);
            } else {
                sideTiles.add(tile);
            }
        }

        int id = FIRST_GHOST_ID;
        for (int i = 0; i < stickCount && !openTiles.isEmpty(); i++) {
            Point tile = openTiles.remove(random.nextInt(openTiles.size()));
            enemies.add(placeEnemy(map, new CreepyStickEnemy(id++, new Point(0, 0)), tile));
            entranceTiles.remove(tile);
            sideTiles.remove(tile);
        }
        for (int i = 0; i < weirdCount; i++) {
            ArrayList<Point> spots = !entranceTiles.isEmpty() && (sideTiles.isEmpty() || random.nextBoolean()) ? entranceTiles : sideTiles;
            if (spots.isEmpty()) {
                spots = openTiles; // no walls to lurk against, so anywhere will do
            }
            if (spots.isEmpty()) {
                break;
            }
            Point tile = spots.remove(random.nextInt(spots.size()));
            enemies.add(placeEnemy(map, new WeirdEnemy(id++, new Point(0, 0)), tile));
            openTiles.remove(tile);
        }
        return enemies;
    }

    // centres the enemy's collision bounds on the tile and adds it to the map
    private static Ghost placeEnemy(Map map, Ghost enemy, Point tile) {
        MapTile mapTile = map.getMapTile((int) tile.x, (int) tile.y);
        float tileWidth = map.getTileset().getScaledSpriteWidth();
        float tileHeight = map.getTileset().getScaledSpriteHeight();
        enemy.setBoundsLocation(
                mapTile.getX() + (tileWidth - enemy.getBounds().getWidth()) / 2f,
                mapTile.getY() + (tileHeight - enemy.getBounds().getHeight()) / 2f);
        map.addNPC(enemy);
        return enemy;
    }

    // tiles with an interact script are the doors
    private static ArrayList<Point> findDoors(Map map) {
        ArrayList<Point> doors = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getMapTile(x, y).getInteractScript() != null) {
                    doors.add(new Point(x, y));
                }
            }
        }
        return doors;
    }

    private static int distanceToNearestDoor(ArrayList<Point> doors, Point tile) {
        int nearest = Integer.MAX_VALUE;
        for (Point door : doors) {
            nearest = Math.min(nearest, (int) Math.max(Math.abs(door.x - tile.x), Math.abs(door.y - tile.y)));
        }
        return nearest;
    }

    // true if any of the 4 tiles next to this one is a wall (or the edge of the map)
    private static boolean isAgainstWall(Map map, int x, int y) {
        int[][] neighbours = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] n : neighbours) {
            MapTile tile = map.getMapTile(x + n[0], y + n[1]);
            if (tile == null || tile.getTileType() == TileType.NOT_PASSABLE) {
                return true;
            }
        }
        return false;
    }

    // walkable tiles that are far enough from every door and don't already have an object (puzzle, note, item) on them
    private static ArrayList<Point> findGhostSpawnTiles(Map map, ArrayList<Point> doors, int minDoorDistance) {
        ArrayList<Point> openTiles = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                MapTile tile = map.getMapTile(x, y);
                if (tile.getTileType() != TileType.PASSABLE || hasObjectOnTile(map, tile)) {
                    continue;
                }
                Point point = new Point(x, y);
                if (distanceToNearestDoor(doors, point) >= minDoorDistance) {
                    openTiles.add(point);
                }
            }
        }
        return openTiles;
    }

    private static boolean hasObjectOnTile(Map map, MapTile tile) {
        for (EnhancedMapTile object : map.getEnhancedMapTiles()) {
            if (object.intersects(tile)) {
                return true;
            }
        }
        return false;
    }

    // prevents DungeonRoomSetup from being instantiated
    private DungeonRoomSetup() { }
}
