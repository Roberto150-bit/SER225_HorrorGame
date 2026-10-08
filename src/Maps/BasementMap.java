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


        PuzzleConfig symbolConfig = new PuzzleConfig(
            "basement_fuse_01",
            PuzzleId.SYMBOL_LOCK,
            PuzzleDifficulty.EASY
        );

        PuzzleObject symbolLock = new PuzzleObject(
            getMapTile(2, 7).getLocation()
        );

        PuzzleConfig radioConfig = new PuzzleConfig(
            "basement_radio_01",
            PuzzleId.TUNE_RADIO,
            PuzzleDifficulty.EASY
        );

        PuzzleObject radioObject = new PuzzleObject(
            getMapTile(4, 7).getLocation()
        );

        PuzzleConfig clockConfig = new PuzzleConfig(
            "basement_clock_01",
            PuzzleId.HAUNTED_CLOCK,
            PuzzleDifficulty.EASY
        );

        PuzzleObject clockObject = new PuzzleObject(
            getMapTile(6, 7).getLocation()
        );
        
        // Configuration for the basement Ritual Sequence puzzle.
        PuzzleConfig ritualConfig = new PuzzleConfig(
            "basement_ritual_01",
            PuzzleId.RITUAL_SEQUENCE,
            PuzzleDifficulty.EASY
        );

        // Temporary interactable object for Ritual Sequence.
        PuzzleObject ritualObject = new PuzzleObject(
            getMapTile(8, 7).getLocation()
        );

        // Configuration for the basement Spirit Mirror puzzle.
        PuzzleConfig mirrorConfig = new PuzzleConfig(
            "basement_mirror_01",
            PuzzleId.SPIRIT_MIRROR,
            PuzzleDifficulty.EASY
        );

        // Temporary interactable object for Spirit Mirror.
        PuzzleObject mirrorObject = new PuzzleObject(
            getMapTile(10, 7).getLocation()
        );

        mirrorObject.setInteractScript(new PuzzleScript(mirrorConfig));
        enhancedMapTiles.add(mirrorObject);

        ritualObject.setInteractScript(new PuzzleScript(ritualConfig));
        enhancedMapTiles.add(ritualObject);


        clockObject.setInteractScript(new PuzzleScript(clockConfig));
        enhancedMapTiles.add(clockObject);

        radioObject.setInteractScript(new PuzzleScript(radioConfig));
        enhancedMapTiles.add(radioObject);

        symbolLock.setInteractScript(new PuzzleScript(symbolConfig));
        enhancedMapTiles.add(symbolLock);

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
