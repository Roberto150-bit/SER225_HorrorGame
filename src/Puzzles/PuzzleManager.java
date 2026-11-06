
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

    // Starts a new game with no puzzles solved and a new code for the final seal.
    public static void reset() {
        progress.reset();
        SealCode.reset();
    }

    // A copy of which puzzles are solved, saved by checkpoints.
    public static java.util.Set<String> snapshot() {
        return progress.snapshot();
    }

    // Puts the solved puzzles back the way they were at a checkpoint.
    // A puzzle solved after the checkpoint can be solved again, but one solved before it can't give XP twice.
    public static void restore(java.util.Set<String> savedCompleted) {
        progress.restore(savedCompleted);
    }

    // Returns the total number of unique puzzles completed.
    public static int getCompletedCount() {
        return progress.getCompletedCount();
    }
}
