package Maps;

import EnhancedMapTiles.*;   
import Level.EnhancedMapTile;
import Level.Map;
import Tilesets.DungeonSecretRoomTileset;
import java.util.ArrayList;
import Scripts.DoorScript;
import Utils.Direction;

public class DungeonSecretRoomMap extends Map {

    public DungeonSecretRoomMap() {
        super("DungeonSecretRoomMap.txt", new DungeonSecretRoomTileset());
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
        // the doorway at the far end of the secret room leads up to the surface
        getMapTile(4, 10).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.DOWN));
    }
}