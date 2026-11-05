
package Level;

public class PlayerProgress {

    private int level;
    private int currentXP;

    private static final int XP_PER_LEVEL = 100;

    // Start each new game session at Level 1.
    public PlayerProgress() {
        level = 1;
        currentXP = 0;
    }

    // Add XP and level up whenever the required amount is reached.
    public void addXP(int amount) {
        if (amount <= 0) {
            return;
        }

        currentXP += amount;

        while (currentXP >= XP_PER_LEVEL) {
            currentXP -= XP_PER_LEVEL;
            level++;
        }
    }

    // Back to Level 1 with no XP, used when a new game starts.
    public void reset() {
        level = 1;
        currentXP = 0;
    }

    // Puts the level and XP back to saved values, used when restarting from a checkpoint.
    public void restore(int savedLevel, int savedXP) {
        level = Math.max(1, savedLevel);
        currentXP = Math.max(0, savedXP);
    }

    // Get the player's current level.
    public int getLevel() {
        return level;
    }

    // Get XP accumulated toward the next level.
    public int getCurrentXP() {
        return currentXP;
    }

    // Get the XP required to reach the next level.
    public int getXPRequired() {
        return XP_PER_LEVEL;
    }

    // Get progress as a percentage for the UI.
    public double getXPPercentage() {
        return (currentXP * 100.0) / XP_PER_LEVEL;
    }
}
