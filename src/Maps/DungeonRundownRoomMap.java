package Maps;

import Enemies.CreepyStickEnemy;
import Enemies.Ghost;
import Enemies.WeirdEnemy;
import EnhancedMapTiles.GreatDoor;
import Level.EnhancedMapTile;
import Level.FlagManager;
import Level.Map;
import Level.MapTile;
import Level.NPC;
import Level.Player;
import Level.Script;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Puzzles.PuzzleManager;
import Scripts.DoorScript;
import Scripts.RoomShiftScript;
import Tilesets.DungeonRundownRoomTileset;
import Utils.Direction;
import Utils.Point;
import java.util.ArrayList;

// The Final Seal room, where the hall ends at the keypad that needs the five code digits found around the dungeon.
// Breaking the seal makes the room shift: the walls draw back, the top chamber grows, and the great door to the
// Necromancer's room rises out of the floor at its far end. Guardians crawl out of the shadows, and the door stays
// chained shut until every enemy in the room is banished.
// The room is 4 tiles taller than the original art; until it shifts, those top rows are solid rock (see DungeonRundownRoomTileset).
public class DungeonRundownRoomMap extends Map {
    // set once the room has shifted, so it stays that way (and is restored with checkpoints)
    public static final String EXPANDED_FLAG = "rundown_room_expanded";

    // where the player arrives from each way in
    public static final int FROM_WATER_ROOM_X = 2, FROM_WATER_ROOM_Y = 10;
    public static final int FROM_HALLWAY_X = 4, FROM_HALLWAY_Y = 5;
    public static final int FROM_BOSS_ROOM_X = 4, FROM_BOSS_ROOM_Y = 3;

    // the keypad sits in the nook on the right of the top chamber
    private static final int SEAL_TILE_X = 5, SEAL_TILE_Y = 6;
    // the great door is centred on this tile, in the new back wall
    private static final int DOOR_TILE_X = 4, DOOR_TILE_Y = 2;
    // the door's foot rests on the top edge of the new floor (in pixels)
    private static final float DOOR_LEFT = 224;
    private static final float DOOR_BOTTOM = 196;

    // where the guardians appear once the door is up: a tall figure in front of it, pale ghosts along the hall,
    // and something lurking at the bottom of the room
    private static final int FIRST_GUARDIAN_ID = 600;
    private static final int[][] GHOST_POSTS = {{2, 7}, {1, 9}};
    private static final int[] STICK_POST = {5, 5};
    // where Dean finds himself when the room settles: right below where the great door rises, so it's in view
    private static final int SHIFT_LANDING_X = 4, SHIFT_LANDING_Y = 3;
    private static final int[] WEIRD_POST = {4, 9};

    // set while the map is being built, so no initializers
    private GreatDoor greatDoor;
    private boolean expanded;
    private boolean shiftStarted;
    private boolean guardiansPlaced;

    public DungeonRundownRoomMap() {
        super("DungeonRundownRoomMap.txt", new DungeonRundownRoomTileset());
        this.playerStartPosition = getMapTile(FROM_WATER_ROOM_X, FROM_WATER_ROOM_Y).getLocation();

        // ghosts start somewhere random each playthrough (only on the floor that exists before the room shifts)
        for (Ghost ghost : DungeonRoomSetup.addRandomGhosts(this, 2, 0)) {
            ghost.setStaysInRoom(true);
        }
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // the Final Seal: a keypad that needs the 5 digit code from the notes found around the dungeon
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, SEAL_TILE_X, SEAL_TILE_Y, DungeonRoomSetup.FINAL_SEAL_PUZZLE, PuzzleId.CODE_LOCK, PuzzleDifficulty.HARD));

        // hidden inside the rock until the room shifts
        greatDoor = new GreatDoor(new Point(DOOR_LEFT, DOOR_BOTTOM - GreatDoor.HEIGHT));
        enhancedMapTiles.add(greatDoor);

        return enhancedMapTiles;
    }

    @Override
    public void loadScripts() {
        // door on the bottom wall back to the water room
        getMapTile(2, 11).setInteractScript(new DoorScript(() -> new DungeonWaterRoomMap(), 3, 3, Direction.DOWN));
    }

    public GreatDoor getGreatDoor() {
        return greatDoor;
    }

    public boolean isExpanded() {
        return expanded;
    }

    // a room the player returns to (or restarts in) is already the shape it was left in
    @Override
    public void setFlagManager(FlagManager flagManager) {
        super.setFlagManager(flagManager);
        if (flagManager != null && flagManager.isFlagSet(EXPANDED_FLAG) && !expanded) {
            expand();
            greatDoor.appear();
            if (!flagManager.isFlagSet(GreatDoor.UNSEALED_FLAG)) {
                placeGuardians();
            }
        }
    }

    // the seal is broken: once the puzzle window is closed and nothing else is going on, the room starts to shift
    @Override
    public void update(Player player) {
        super.update(player);
        if (!expanded && !shiftStarted && PuzzleManager.isCompleted(DungeonRoomSetup.FINAL_SEAL_PUZZLE)
                && !puzzleUI.isOpen() && !textbox.isActive() && (activeScript == null || !activeScript.isActive())) {
            shiftStarted = true;
            setActiveScript(new RoomShiftScript(this));
        }
    }

    // swaps every tile for the grown version of the room (done while the screen is black) and puts in the great door's tile
    public void expand() {
        if (expanded) {
            return;
        }
        expanded = true;
        if (flagManager != null) {
            flagManager.addFlag(EXPANDED_FLAG, true);
        }

        DungeonRundownRoomTileset grown = new DungeonRundownRoomTileset(true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                MapTile old = getMapTile(x, y);
                MapTile tile = grown.getTile(y * width + x).build(old.getX(), old.getY());
                if (old.getInteractScript() != null) {
                    tile.setInteractScript(old.getInteractScript());
                }
                setMapTile(x, y, tile);
            }
        }
        tileset = grown;

        // the great door: stand below it, face up, press E
        Script door = new DoorScript(() -> new DungeonSecretRoomMap(), 4, 8, Direction.UP)
                .lockedUntil(() -> greatDoor.isUnsealed(), this::lockedDoorMessage);
        getMapTile(DOOR_TILE_X, DOOR_TILE_Y).setInteractScript(door);
        initializeScript(door);
    }

    private String lockedDoorMessage() {
        if (greatDoor.isRising()) {
            return "The door is still rising.";
        }
        int remaining = greatDoor.getGuardiansRemaining();
        if (remaining == 0) {
            return "The chains are loosening...";
        }
        return "Black iron chains bind the great door shut.\n"
                + (remaining == 1 ? "One enemy still lurks in this room." : remaining + " enemies still lurk in this room.");
    }

    // after the shift, Dean stands just below where the door is about to rise (facing it), so the camera frames it
    public void placePlayerForDoorRise(Player player) {
        MapTile landing = getMapTile(SHIFT_LANDING_X, SHIFT_LANDING_Y);
        float tileSize = getTileset().getScaledSpriteWidth();
        float offsetX = player.getBounds().getX() - player.getX();
        float offsetY = player.getBounds().getY() - player.getY();
        player.setLocation(
                landing.getX() + (tileSize - player.getBounds().getWidth()) / 2f - offsetX,
                landing.getY() + (tileSize - player.getBounds().getHeight()) / 2f - offsetY);
        player.setFacingDirection(Direction.UP);
    }

    // the door rises out of the floor, and the guardians crawl out once it's up
    public void raiseGreatDoor() {
        greatDoor.rise(() -> {
            placeGuardians();
            // the door standing with its guardians around it is a checkpoint
            for (Level.GameListener listener : getListeners()) {
                listener.onCheckpointReached();
            }
        });
    }

    // the new guardians, plus every enemy still in the room, have to be banished before the door opens
    private void placeGuardians() {
        if (guardiansPlaced) {
            return;
        }
        guardiansPlaced = true;
        ArrayList<Ghost> guardians = new ArrayList<>();
        for (NPC npc : getNPCs()) {
            if (npc instanceof Ghost && ((Ghost) npc).getHealth() > 0) {
                guardians.add((Ghost) npc);
            }
        }
        int id = FIRST_GUARDIAN_ID;
        guardians.add(DungeonRoomSetup.addEnemyAt(this, new CreepyStickEnemy(id++, new Point(0, 0)), STICK_POST[0], STICK_POST[1]));
        for (int[] post : GHOST_POSTS) {
            guardians.add(DungeonRoomSetup.addEnemyAt(this, new Ghost(id++, new Point(0, 0)), post[0], post[1]));
        }
        guardians.add(DungeonRoomSetup.addEnemyAt(this, new WeirdEnemy(id++, new Point(0, 0)), WEIRD_POST[0], WEIRD_POST[1]));
        for (Ghost guardian : guardians) {
            guardian.setStaysInRoom(true);
        }
        greatDoor.setGuardians(guardians);
    }

    @Override
    public String getObjectiveText() {
        if (greatDoor == null || !greatDoor.isSealed()) {
            return null;
        }
        int remaining = greatDoor.getGuardiansRemaining();
        return remaining == 1 ? "The great door is sealed - 1 enemy remains"
                : "The great door is sealed - " + remaining + " enemies remain";
    }
}
