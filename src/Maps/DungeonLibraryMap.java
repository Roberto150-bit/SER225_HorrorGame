package Maps;

import GameObject.Relic;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Level.EnhancedMapTile;
import Level.Map;
import Scripts.DoorScript;
import Tilesets.DungeonLibraryTileset;
import Utils.Direction;
import java.util.ArrayList;


public class DungeonLibraryMap extends Map {

    public DungeonLibraryMap() {
        super("DungeonLibraryMap.txt", new DungeonLibraryTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        // ghosts start somewhere random each playthrough
        // the big ghost guards the bloody eye relic and drops it when banished
        DungeonRoomSetup.addRandomGhosts(this, 1, 0).get(0).setDrop(Relic.BloodyEye::new);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // solving this unlocks the basic room door in the hallway
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 6, 6, DungeonRoomSetup.LIBRARY_PUZZLE, PuzzleId.HAUNTED_CLOCK, PuzzleDifficulty.MEDIUM));

        // third digit of the final seal's code
        enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 2, 8, 2));

        // page 3 of the journal's story
        enhancedMapTiles.add(DungeonRoomSetup.storyNote(this, 7, 4, 3));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the bottom wall back to the hallway
        getMapTile(3, 10).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 4, 6, Direction.RIGHT));
    }
}