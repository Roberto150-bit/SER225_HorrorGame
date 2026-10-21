package Maps;

import EnhancedMapTiles.*;   
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
        getMapTile(1, 1).setInteractScript(new DoorScript(() -> new BasementMap(), 12, 4, Direction.RIGHT));

        getMapTile(8, 4).setInteractScript(new DoorScript(() -> new DungeonCellMap(), 2, 3, Direction.RIGHT));

    }

}