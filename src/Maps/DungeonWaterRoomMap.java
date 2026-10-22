package Maps;

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
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //nothing here yet, but this is where you would add any enhanced map tiles to the map. 

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the right wall next to the stairs stand on the stairs at (12, 4), face right, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        
        // HERE -------------------------------------------------------------
        getMapTile(9, 5).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 1, 3, Direction.LEFT));

        getMapTile(3, 2).setInteractScript(new DoorScript(() -> new DungeonRundownRoomMap(), 2, 6, Direction.LEFT));

    }
}