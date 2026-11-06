
package Puzzles;

import java.util.HashSet;
import java.util.Set;

public class PuzzleProgress {

    // Tracks completed puzzle instances using their unique IDs.
    private final Set<String> completed = new HashSet<>();

    // Returns true only when this instance is completed for the first time.
    public boolean complete(String instanceId) {
        return completed.add(instanceId);
    }

    // Checks if a specific puzzle instance has been completed.
    public boolean isCompleted(String instanceId) {
        return completed.contains(instanceId);
    }

    // Forgets every completed puzzle, used when a new game starts.
    public void reset() {
        completed.clear();
    }

    // A copy of every completed puzzle id, saved by checkpoints.
    public Set<String> snapshot() {
        return new HashSet<>(completed);
    }

    // Puts the completed puzzles back to a saved set, used when restarting from a checkpoint.
    public void restore(Set<String> savedCompleted) {
        completed.clear();
        completed.addAll(savedCompleted);
    }

    // Returns the total number of completed puzzle instances.
    public int getCompletedCount() {
        return completed.size();
    }
}
