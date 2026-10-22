package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import static Puzzles.PuzzleManager.getCompletedCount;
import Scripts.DoorScript;
import Tilesets.DungeonSecretRoomTileset;
import Utils.Direction;
import java.util.ArrayList;

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
        // door on the right wall next to the stairs stand on the stairs at (12, 4), face right, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        
        // HERE -------------------------------------------------------------
        getMapTile(4, 10).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 5, 4, Direction.LEFT));


        System.out.println(getCompletedCount());
        getMapTile(13, 4).setInteractScript(new DoorScript(
            () -> getCompletedCount() >= 5
                    ? new DungeonHallwaySecretDoorRevealedMap()
                    : new DungeonHallwayMap(),
            4, 7, Direction.UP   // <-- replace with the spawn tile + facing you want in the hallway
        ));
    }
}