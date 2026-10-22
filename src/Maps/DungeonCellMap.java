package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import static Puzzles.PuzzleManager.getCompletedCount;
import Scripts.DoorScript;
import Tilesets.DungeonCellTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonCellMap extends Map {

    public DungeonCellMap() {
        super("DungeonCellMap.txt", new DungeonCellTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        // ghosts start somewhere random each playthrough
        DungeonRoomSetup.addRandomGhosts(this, 0, 1);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // solving this unlocks the library door in the hallway
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 6, 6, DungeonRoomSetup.CELL_PUZZLE, PuzzleId.TUNE_RADIO, PuzzleDifficulty.EASY));

        // second digit of the final seal's code
        enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 3, 4, 1));

        // page 2 of the journal's story
        enhancedMapTiles.add(DungeonRoomSetup.storyNote(this, 5, 3, 2));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the top wall back to the hallway
        getMapTile(2, 2).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 7, 4, Direction.LEFT));

        System.out.println(getCompletedCount());
        getMapTile(13, 4).setInteractScript(new DoorScript(
            () -> getCompletedCount() >= 5
                    ? new DungeonHallwaySecretDoorRevealedMap()
                    : new DungeonHallwayMap(),
            4, 7, Direction.UP   // <-- replace with the spawn tile + facing you want in the hallway
        ));
    }
}