package Maps;

import Engine.MusicManager;
import Engine.MusicPlayer;
import GameObject.Knife;
import Level.*;
import NPCs.MsValeNPC;
import Puzzles.PuzzleDifficulty;
import Puzzles.PuzzleId;
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
        enhancedMapTiles.add(DungeonRoomSetup.puzzle(this, 10, 2, DungeonRoomSetup.BASEMENT_PUZZLE, PuzzleId.SYMBOL_LOCK, PuzzleDifficulty.EASY));

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
        // stairs on the right wall up to the dungeon hallway -- stand on the stairs at (12, 4), face right, and press E
        // the dungeon is the training ground, so the only way out to the surface is through the secret room at the end
        getMapTile(13, 4).setInteractScript(new DoorScript(() -> new DungeonHallwayMap(), 1, 2, Direction.DOWN)
                .lockedUntilSolved(DungeonRoomSetup.BASEMENT_PUZZLE, "The way up is sealed.\nSolve the lock in this room to open it."));
    }

}
