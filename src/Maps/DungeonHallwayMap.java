package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import Scripts.DoorScript;
import Tilesets.DungeonHallwayTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonHallwayMap extends Map {

    public DungeonHallwayMap() {
        super("DungeonHallwayMap.txt", new DungeonHallwayTileset());
        this.playerStartPosition = getMapTile(6, 7).getLocation();

        // ghosts start somewhere random each playthrough
        DungeonRoomSetup.addRandomGhosts(this, 0, 1);
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // first digit of the final seal's code, lying in the main corridor
        enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 6, 4, 0));

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // stairs on the top wall up to the basement
        getMapTile(1, 1).setInteractScript(new DoorScript(() -> new BasementMap(), 12, 4, Direction.LEFT));

        // door on the right wall to the cell
        getMapTile(8, 4).setInteractScript(new DoorScript(() -> new DungeonCellMap(), 2, 3, Direction.DOWN));

        // door on the left wall to the water room
        getMapTile(0, 4).setInteractScript(new DoorScript(() -> new DungeonWaterRoomMap(), 8, 5, Direction.LEFT)
                .lockedUntilSolved(DungeonRoomSetup.BASIC_ROOM_PUZZLE, "Water seeps under this sealed door.\nThe room at the end of the hall must be solved first."));

        // door on the left side of the lower corridor to the library
        getMapTile(3, 6).setInteractScript(new DoorScript(() -> new DungeonLibraryMap(), 3, 9, Direction.UP)
                .lockedUntilSolved(DungeonRoomSetup.CELL_PUZZLE, "The library door is sealed.\nSomething in the cell might open it."));

        // door at the bottom of the lower corridor to the basic room
        getMapTile(4, 8).setInteractScript(new DoorScript(() -> new DungeonBasicRoomMap(), 3, 7, Direction.UP)
                .lockedUntilSolved(DungeonRoomSetup.LIBRARY_PUZZLE, "This door won't budge.\nThe library holds the answer."));
    }

}