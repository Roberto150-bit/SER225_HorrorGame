package Maps;

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
        getMapTile(3, 10).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 6, 4, Direction.LEFT));

    }
}