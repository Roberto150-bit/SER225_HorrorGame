package Maps;

import Enemies.Ghost;
import Enemies.SmallGhost;
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
    private static final int FIRST_GHOST_ID = 100;

    private static final Random random = new Random();

    // a puzzle the player interacts with (E) on the given tile, drawn with that puzzle type's sprite
    public static PuzzleObject puzzle(Map map, int tileX, int tileY, String instanceId, PuzzleId type, PuzzleDifficulty difficulty) {
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

    // places ghosts on random open floor tiles, a new layout every time the room is built (each playthrough)
    // call at the end of a map's constructor, after its doors and objects are set up, so they can be avoided
    // returns the ghosts it placed (big ones first), so a room can give one of them an item to drop
    public static ArrayList<Ghost> addRandomGhosts(Map map, int ghostCount, int smallGhostCount) {
        ArrayList<Ghost> ghosts = new ArrayList<>();
        ArrayList<Point> openTiles = findGhostSpawnTiles(map, GHOST_DOOR_DISTANCE);
        // small rooms might not have enough space that far from the doors, so allow closer spots if needed
        if (openTiles.size() < ghostCount + smallGhostCount) {
            openTiles = findGhostSpawnTiles(map, GHOST_DOOR_DISTANCE - 1);
        }

        int id = FIRST_GHOST_ID;
        for (int i = 0; i < ghostCount + smallGhostCount && !openTiles.isEmpty(); i++) {
            Point tile = openTiles.remove(random.nextInt(openTiles.size()));
            Point location = map.getMapTile((int) tile.x, (int) tile.y).getLocation();
            Ghost ghost = i < ghostCount ? new Ghost(id++, location) : new SmallGhost(id++, location);
            map.addNPC(ghost);
            ghosts.add(ghost);
        }
        return ghosts;
    }

    // walkable tiles that are far enough from every door and don't already have an object (puzzle, note, item) on them
    private static ArrayList<Point> findGhostSpawnTiles(Map map, int minDoorDistance) {
        ArrayList<Point> doors = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getMapTile(x, y).getInteractScript() != null) {
                    doors.add(new Point(x, y));
                }
            }
        }

        ArrayList<Point> openTiles = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                MapTile tile = map.getMapTile(x, y);
                if (tile.getTileType() != TileType.PASSABLE || hasObjectOnTile(map, tile)) {
                    continue;
                }
                boolean nearDoor = false;
                for (Point door : doors) {
                    if (Math.max(Math.abs(door.x - x), Math.abs(door.y - y)) < minDoorDistance) {
                        nearDoor = true;
                        break;
                    }
                }
                if (!nearDoor) {
                    openTiles.add(new Point(x, y));
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
