package Maps;

import GameObject.Knife;
import Level.*;
import NPCs.MsValeNPC;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
import static Puzzles.PuzzleManager.getCompletedCount;
import Scripts.DoorScript;
import Tilesets.BasementTileset;
import Utils.Direction;
import java.util.ArrayList;


public class BasementMap extends Map {
    public BasementMap() {
        super("BasementMap.txt", new BasementTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();
    }

    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        // Knife
        enhancedMapTiles.add(new Knife(getMapTile(8, 4).getLocation()));

        // the relics (eye, ear, crucifix) aren't lying around anymore -- ghosts in the dungeon carry them (see the dungeon room maps)
        

        // first page of the journal's story -- the other four story notes are spread through the dungeon rooms
        enhancedMapTiles.add(DungeonRoomSetup.storyNote(this, 6, 6, 1));



        // tutorial puzzle -- solving it unlocks the stairs up to the dungeon
        // the other puzzles are spread through the dungeon rooms (see DungeonRoomSetup)
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 10, 2, DungeonRoomSetup.BASEMENT_PUZZLE, PuzzleId.SYMBOL_LOCK, PuzzleDifficulty.MEDIUM));

        return enhancedMapTiles;
    }

    @Override
    public ArrayList<NPC> loadNPCs() {
        ArrayList<NPC> npcs = new ArrayList<>();

        // Mrs. Vale stands by the desk on the left wall, facing the player's start position
        MsValeNPC msVale = new MsValeNPC(1, getMapTile(5, 4).getLocation());
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
        System.out.println(getCompletedCount());
        getMapTile(13, 4).setInteractScript(new DoorScript(
            () -> getCompletedCount() >= 5
                    ? new DungeonHallwaySecretDoorRevealedMap()
                    : new DungeonHallwayMap(),
            1, 2, Direction.RIGHT));

        //getMapTile(13, 4).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 1, 2, Direction.RIGHT));
    }

}
