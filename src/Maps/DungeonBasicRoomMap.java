package Maps;

import GameObject.Relic;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Level.EnhancedMapTile;
import Level.Map;
import Scripts.DoorScript;
import Tilesets.DungeonBasicRoomTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonBasicRoomMap extends Map {

    public DungeonBasicRoomMap() {
        super("Room4Tileset.txt", new DungeonBasicRoomTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        // ghosts start somewhere random each playthrough
        // the big ghost guards the ear relic and drops it when banished
        DungeonRoomSetup.addRandomGhosts(this, 1, 1).get(0).setDrop(Relic.Ear::new);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // solving this unlocks the water room door in the hallway
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 6, 4, DungeonRoomSetup.BASIC_ROOM_PUZZLE, PuzzleId.RITUAL_SEQUENCE, PuzzleDifficulty.MEDIUM));

        // fourth digit of the final seal's code
        enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 2, 6, 3));

        // page 4 of the journal's story
        enhancedMapTiles.add(DungeonRoomSetup.storyNote(this, 5, 6, 4));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the bottom wall back to the hallway
        getMapTile(3, 8).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 4, 7, Direction.UP));
    }
}