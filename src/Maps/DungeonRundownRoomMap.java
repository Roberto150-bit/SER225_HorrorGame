package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import static Puzzles.PuzzleManager.getCompletedCount;
import Scripts.DoorScript;
import Tilesets.DungeonRundownRoomTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonRundownRoomMap extends Map {

    public DungeonRundownRoomMap() {
        super("DungeonRundownRoomMap.txt", new DungeonRundownRoomTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        // ghosts start somewhere random each playthrough
        DungeonRoomSetup.addRandomGhosts(this, 2, 0);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // the "final boss": a keypad that needs the 5 digit code from the notes found around the dungeon
        // it sits in the nook beside the hidden passage, and solving it opens the passage to the secret room
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 5, 2, DungeonRoomSetup.FINAL_SEAL_PUZZLE, PuzzleId.CODE_LOCK, PuzzleDifficulty.HARD));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the bottom wall back to the water room
        getMapTile(2, 7).setInteractScript(new DoorScript(() -> new DungeonWaterRoomMap(), 3, 3, Direction.DOWN));

        // hidden passage to the secret room, on the back wall of the nook in the top right corner (stand at (5, 1) and face up)
        // this is the deepest room in the dungeon, so it stays locked until the final seal is broken
        getMapTile(5, 0).setInteractScript(new DoorScript(() -> new DungeonSecretRoomMap(), 4, 2, Direction.DOWN)
                .lockedUntilSolved(DungeonRoomSetup.FINAL_SEAL_PUZZLE, "Cold air seeps through a crack in the wall.\nThe seal beside it must be broken."));

        System.out.println(getCompletedCount());
        getMapTile(13, 4).setInteractScript(new DoorScript(
            () -> getCompletedCount() >= 5
                    ? new DungeonHallwaySecretDoorRevealedMap()
                    : new DungeonHallwayMap(),
            4, 7, Direction.UP   // <-- replace with the spawn tile + facing you want in the hallway
        ));
    }
}