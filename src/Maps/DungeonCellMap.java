package Maps;

import EnhancedMapTiles.*;   
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.DungeonCellTileset;
import java.util.ArrayList;

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
}