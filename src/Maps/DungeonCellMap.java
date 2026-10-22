package Maps;

import EnhancedMapTiles.*;   
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.DungeonCellTileset;
import java.util.ArrayList;
import Scripts.DoorScript;
import Utils.Direction;

public class DungeonCellMap extends Map {

    public DungeonCellMap() {
        super("DungeonCellMap.txt", new DungeonCellTileset());
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
        getMapTile(2, 2).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 7, 4, Direction.LEFT));

    }
}