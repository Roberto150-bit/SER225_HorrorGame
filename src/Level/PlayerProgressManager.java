
package Level;

import Engine.SoundPlayer;

public class PlayerProgressManager {

    // Shared player progress throughout the current game session.
    private static final PlayerProgress progress = new PlayerProgress();

    private static final String LEVEL_UP_SOUND = "levelUp.wav";

    // Award experience points to the player.
    public static void awardXP(int amount) {
        int previousLevel = progress.getLevel();
        progress.addXP(amount);

        // play the level up sound once, even if multiple levels were gained
        if (progress.getLevel() > previousLevel) {
            SoundPlayer.play(LEVEL_UP_SOUND);
        }
    }

    // Starts a new game at Level 1 with no XP.
    public static void reset() {
        progress.reset();
    }

    // Puts the level and XP back to what they were at a checkpoint.
    public static void restore(int level, int currentXP) {
        progress.restore(level, currentXP);
    }

    // Returns the player's current level.
    public static int getLevel() {
        return progress.getLevel();
    }

    // Returns XP earned toward the next level.
    public static int getCurrentXP() {
        return progress.getCurrentXP();
    }

    // Returns the XP required for the next level.
    public static int getXPRequired() {
        return progress.getXPRequired();
    }

    // Returns XP progress as a percentage.
    public static double getXPPercentage() {
        return progress.getXPPercentage();
    }
}
