package Level;

import Enemies.Ghost;
import Engine.HotbarUI;
import Engine.ItemUse;
import Engine.UI;
import Puzzles.PuzzleManager;
import Puzzles.SealCode;
import Utils.Direction;

import java.util.Set;

// A saved moment in Chapter 1 that the player can restart from after dying.
// It keeps everything the player has earned up to that point -- which room they were in and where they stood,
// solved puzzles (and so which doors are open), collected items, found code digits, journal pages, XP and level --
// using the game's existing state rather than a save file, so it only lasts for the current playthrough.
// Rooms themselves aren't saved: restarting builds them fresh, and the saved flags keep picked up items gone.
public class Checkpoint {
    private final Class<? extends Map> roomType;
    private final float playerX;
    private final float playerY;
    private final Direction facingDirection;

    private final Set<String> completedPuzzles;
    private final boolean[] foundCodeDigits;
    private final int level;
    private final int currentXP;
    private final FlagManager flags;
    private final HotbarUI hotbar;
    private final UI journal;

    private Checkpoint(Map room, Player player, FlagManager flagManager) {
        roomType = room.getClass();
        playerX = player.getX();
        playerY = player.getY();
        facingDirection = player.getFacingDirection();

        completedPuzzles = PuzzleManager.snapshot();
        foundCodeDigits = SealCode.snapshotFound();
        level = PlayerProgressManager.getLevel();
        currentXP = PlayerProgressManager.getCurrentXP();
        flags = flagManager.copy();
        hotbar = room.getHotbarUI().copy();
        journal = room.getUI().copy();
    }

    // saves the current state of the game
    public static Checkpoint capture(Map room, Player player, FlagManager flagManager) {
        return new Checkpoint(room, player, flagManager);
    }

    // puts puzzles, code digits, XP and drops back the way they were
    // (the room, player, flags, hotbar and journal are handed out by the methods below, as fresh copies each time,
    // so the same checkpoint can be restarted from again and again)
    public void restoreProgress() {
        PuzzleManager.restore(completedPuzzles);
        SealCode.restoreFound(foundCodeDigits);
        PlayerProgressManager.restore(level, currentXP);
        // if the player didn't have the bow yet, the next ghost they banish drops it again
        Ghost.setBowDropped(hotbar.hasItem(ItemUse.RANGED));
        Player.setHealth(Player.MAX_HEALTH);
    }

    // a fresh copy of the room the checkpoint was saved in
    public Map createRoom() {
        try {
            return roomType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not rebuild checkpoint room " + roomType.getName(), e);
        }
    }

    public FlagManager copyFlags() {
        return flags.copy();
    }

    public HotbarUI copyHotbar() {
        return hotbar.copy();
    }

    public UI copyJournal() {
        return journal.copy();
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public Class<? extends Map> getRoomType() {
        return roomType;
    }
}
