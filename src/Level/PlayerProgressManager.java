
package Level;

public class PlayerProgressManager {

    // Shared player progress throughout the current game session.
    private static final PlayerProgress progress = new PlayerProgress();

    // Award experience points to the player.
    public static void awardXP(int amount) {
        progress.addXP(amount);
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
