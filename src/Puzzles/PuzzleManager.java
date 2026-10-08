
package Puzzles;

public class PuzzleManager {

    // Shared progress across all maps in the current game session.
    private static final PuzzleProgress progress = new PuzzleProgress();

    // Records a successful puzzle result.
    // Returns true only for the first completion.
    public static boolean recordResult(PuzzleResult result) {
        if (result == null || !result.isCompleted()) {
            return false;
        }

        return progress.complete(result.getInstanceId());
    }

    // Checks whether a particular puzzle has been solved.
    public static boolean isCompleted(String instanceId) {
        return progress.isCompleted(instanceId);
    }

    // Returns the total number of unique puzzles completed.
    public static int getCompletedCount() {
        return progress.getCompletedCount();
    }
}
