package Maps;

import Level.EnhancedMapTile;
import Level.Map;
import Puzzles.SealCode;
import Scripts.DoorScript;
import Tilesets.DungeonHallwaySecretDoorRevealedTileset;
import Utils.Direction;
import java.util.ArrayList;

// The hallway once all five puzzles are solved: a secret door has appeared in the back wall,
// a shortcut to the Final Seal room (where the great door to the Necromancer's room rises).
public class DungeonHallwaySecretDoorRevealedMap extends Map {

    public DungeonHallwaySecretDoorRevealedMap() {
        super("DungeonHallwaySecretDoorRevealedMap.txt", new DungeonHallwaySecretDoorRevealedTileset());
        this.playerStartPosition = getMapTile(6, 7).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // the first code digit lies in the hallway; if the player walked past it before every puzzle was solved,
        // it's still here, otherwise the final seal couldn't be opened
        if (!SealCode.isFound(0)) {
            enhancedMapTiles.add(DungeonRoomSetup.codeNote(this, 6, 4, 0));
        }

        return enhancedMapTiles;
    }

    // same doors (and arrival spots) as DungeonHallwayMap, plus the secret door
    // every puzzle is solved by the time this version of the hallway exists, so none of them are locked
    @Override
    public void loadScripts() {
        getMapTile(1, 1).setInteractScript(new DoorScript(() -> new BasementMap(), 12, 4, Direction.LEFT));
        getMapTile(8, 4).setInteractScript(new DoorScript(() -> new DungeonCellMap(), 2, 3, Direction.DOWN));
        getMapTile(0, 4).setInteractScript(new DoorScript(() -> new DungeonWaterRoomMap(), 8, 5, Direction.LEFT));
        getMapTile(3, 6).setInteractScript(new DoorScript(() -> new DungeonLibraryMap(), 3, 9, Direction.UP));
        getMapTile(4, 8).setInteractScript(new DoorScript(() -> new DungeonBasicRoomMap(), 4, 6, Direction.RIGHT));
        // the secret door: a shortcut into the top of the Final Seal room
        getMapTile(5, 3).setInteractScript(new DoorScript(() -> new DungeonRundownRoomMap(),
                DungeonRundownRoomMap.FROM_HALLWAY_X, DungeonRundownRoomMap.FROM_HALLWAY_Y, Direction.DOWN));
    }

}
