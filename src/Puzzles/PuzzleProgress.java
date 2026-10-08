
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

    // Returns the total number of completed puzzle instances.
    public int getCompletedCount() {
        return completed.size();
    }
}
