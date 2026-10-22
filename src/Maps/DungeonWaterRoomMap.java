package Maps;

import GameObject.Relic;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Level.EnhancedMapTile;
import Level.Map;
import Scripts.DoorScript;
import Tilesets.DungeonWaterRoomTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonWaterRoomMap extends Map {

    public DungeonWaterRoomMap() {
        super("DungeonWaterRoomMap.txt", new DungeonWaterRoomTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        // ghosts start somewhere random each playthrough
        // the big ghost guards the crucifix relic and drops it when banished
        DungeonRoomSetup.addRandomGhosts(this, 1, 1).get(0).setDrop(Relic.Crucifix::new);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // solving this unlocks the door to the rundown room
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 7, 7, DungeonRoomSetup.WATER_ROOM_PUZZLE, PuzzleId.SPIRIT_MIRROR, PuzzleDifficulty.HARD));

        // fifth and last digit of the final seal's code
        enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 2, 7, 4));

        // page 5 of the journal's story
        enhancedMapTiles.add(DungeonRoomSetup.storyNote(this, 7, 3, 5));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the right wall back to the hallway
        getMapTile(9, 5).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 1, 4, Direction.RIGHT));

        // door on the top wall to the rundown room
        getMapTile(3, 2).setInteractScript(new DoorScript(() -> new DungeonRundownRoomMap(), 2, 6, Direction.UP)
                .lockedUntilSolved(DungeonRoomSetup.WATER_ROOM_PUZZLE, "Something holds this door shut.\nThe mirror in this room is watching."));
    }
}