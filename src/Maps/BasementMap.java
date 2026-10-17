package Maps;

import Engine.ImageLoader;
import EnhancedMapTiles.CollectibleObject;
import EnhancedMapTiles.NoteObject;
import EnhancedMapTiles.PuzzleObject;
import Level.*;
import NPCs.MsValeNPC;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import Scripts.DoorScript;
import Scripts.NoteScript;
import Scripts.PuzzleScript;
import Tilesets.BasementTileset;
import Utils.Direction;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


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
        enhancedMapTiles.add(new CollectibleObject(getMapTile(11, 6).getLocation(), "crucifix.gif", 256));
        

        NoteObject noteObject1 = new NoteObject(getMapTile(4, 6).getLocation());// --------------------------------
        NoteObject noteObject2 = new NoteObject(getMapTile(5, 6).getLocation());// --------------------------------
        NoteObject noteObject3 = new NoteObject(getMapTile(6, 6).getLocation());// --------------------------------
        NoteObject noteObject4 = new NoteObject(getMapTile(7, 6).getLocation());// --------------------------------
        NoteObject noteObject5 = new NoteObject(getMapTile(8, 6).getLocation());// --------------------------------
        
        CollectibleObject collectibleObject = new CollectibleObject(getMapTile(6, 7).getLocation());
        noteObject1.setIsUncollidable(true);
        noteObject2.setIsUncollidable(true);
        noteObject3.setIsUncollidable(true);
        noteObject4.setIsUncollidable(true);
        noteObject5.setIsUncollidable(true);


        BufferedImage noteItemImage = ImageLoader.load("Note.png");
        noteObject1.setInteractScript(new NoteScript(noteItemImage));
        noteObject2.setInteractScript(new NoteScript(noteItemImage));
        noteObject3.setInteractScript(new NoteScript(noteItemImage));
        noteObject4.setInteractScript(new NoteScript(noteItemImage));
        noteObject5.setInteractScript(new NoteScript(noteItemImage));

        enhancedMapTiles.add(collectibleObject);
        enhancedMapTiles.add(noteObject1);
        enhancedMapTiles.add(noteObject2);
        enhancedMapTiles.add(noteObject3);
        enhancedMapTiles.add(noteObject4);
        enhancedMapTiles.add(noteObject5);



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
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        // Mrs. Vale stands by the desk on the left wall, facing the player's start position
        MsValeNPC msVale = new MsValeNPC(1, getMapTile(2, 4).getLocation());
        npcs.add(msVale);

        return npcs;
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
