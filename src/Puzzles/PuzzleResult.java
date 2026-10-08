package Puzzles;

public class PuzzleResult {

    private final PuzzleConfig config;
    private final boolean completed;

    // Represents the result of one puzzle interaction.
    public PuzzleResult(PuzzleConfig config, boolean completed) {
        if (config == null) {
            throw new IllegalArgumentException("Puzzle config cannot be null");
        }

        this.config = config;
        this.completed = completed;
    }

    // Identifies which puzzle instance produced this result.
    public String getInstanceId() {
        return config.getInstanceId();
    }

    // Identifies the puzzle type.
    public PuzzleId getPuzzleType() {
        return config.getPuzzleType();
    }

    // Returns the difficulty of this puzzle instance.
    public PuzzleDifficulty getDifficulty() {
        return config.getDifficulty();
    }

    // Returns true if the player solved the puzzle.
    public boolean isCompleted() {
        return completed;
    }
}
