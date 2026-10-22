package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Level.Player;
import Puzzles.PuzzleManager;
import Scripts.DoorScript;
import Tilesets.DungeonHallwaySecretDoorRevealedTileset;
import Utils.Direction;
import java.util.ArrayList;

public class DungeonHallwaySecretDoorRevealedMap extends Map {
    
    public DungeonHallwaySecretDoorRevealedMap() {
        super("DungeonHallwaySecretDoorRevealedMap.txt", new DungeonHallwaySecretDoorRevealedTileset());
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

        getMapTile(0, 4).setInteractScript(new DoorScript(() -> new DungeonWaterRoomMap(), 8, 5 , Direction.RIGHT));

        getMapTile(3, 6).setInteractScript(new DoorScript(() -> new DungeonLibraryMap(), 2, 9 , Direction.RIGHT));

        getMapTile(4, 8).setInteractScript(new DoorScript(() -> new DungeonBasicRoomMap(), 4, 6, Direction.RIGHT));
        //only if 5 puzzle completed
        getMapTile(5, 3).setInteractScript(new DoorScript(() -> new DungeonSecretRoomMap(), 4, 8, Direction.RIGHT));
    }

}