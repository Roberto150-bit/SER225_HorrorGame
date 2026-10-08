package Maps;

import EnhancedMapTiles.CollectibleObject;
import Level.*;
import Scripts.DoorScript;
import Tilesets.BasementTileset;
import Utils.Direction;
import java.util.ArrayList;
import EnhancedMapTiles.PuzzleObject;
import Scripts.PuzzleScript;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleId;
import Puzzles.PuzzleDifficulty;


public class BasementMap extends Map {

    public BasementMap() {
        super("BasementMap.txt", new BasementTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // source images are 256x256, scaled down to 32x32 in the world
        enhancedMapTiles.add(new CollectibleObject(getMapTile(3, 3).getLocation(), "BloodyEye.gif", 256));
        enhancedMapTiles.add(new CollectibleObject(getMapTile(9, 6).getLocation(), "ear.png", 256));

        
        // Configuration for the first basement puzzle.
        PuzzleConfig config = new PuzzleConfig(
            "basement_fuse_01",
            PuzzleId.SYMBOL_LOCK,
            PuzzleDifficulty.EASY
        );

        // Creates the puzzle object and assigns its configuration.
        PuzzleObject puzzleObject = new PuzzleObject(
            getMapTile(2, 7).getLocation()
        );

        puzzleObject.setInteractScript(new PuzzleScript(config));
        enhancedMapTiles.add(puzzleObject);

        return enhancedMapTiles;
    }

    @Override
    public void loadScripts() {
        // door on the left wall stand next to it, face left, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        getMapTile(0, 7).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.RIGHT));
        getMapTile(0, 8).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.RIGHT));

        // door on the right wall next to the stairs stand on the stairs at (12, 4), face right, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        getMapTile(13, 4).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.RIGHT));
    }

}
