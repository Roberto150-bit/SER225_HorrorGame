
package Puzzles;

public class PuzzleConfig {

    private final String instanceId;
    private final PuzzleId puzzleType;
    private final PuzzleDifficulty difficulty;

    // Defines one specific puzzle placed in the world.
    public PuzzleConfig(String instanceId, PuzzleId puzzleType,
                        PuzzleDifficulty difficulty) {

        if (instanceId == null || instanceId.trim().isEmpty()
                || puzzleType == null || difficulty == null) {
            throw new IllegalArgumentException("Invalid puzzle configuration");
        }

        this.instanceId = instanceId;
        this.puzzleType = puzzleType;
        this.difficulty = difficulty;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public PuzzleId getPuzzleType() {
        return puzzleType;
    }

    public PuzzleDifficulty getDifficulty() {
        return difficulty;
    }
}
