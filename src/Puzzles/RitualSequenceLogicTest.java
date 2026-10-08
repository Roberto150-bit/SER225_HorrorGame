
package Puzzles;

public class RitualSequenceLogicTest {

    public static void main(String[] args) {

        testSequence(PuzzleDifficulty.EASY,
            new int[]{0, 2, 1}, 3, 1200);

        testSequence(PuzzleDifficulty.MEDIUM,
            new int[]{1, 3, 0, 2}, 4, 900);

        testSequence(PuzzleDifficulty.HARD,
            new int[]{2, 4, 1, 3, 0}, 5, 600);

        System.out.println("All Ritual Sequence tests passed!");
    }

    // Test sequence progress, mistakes, completion, and reset.
    private static void testSequence(
            PuzzleDifficulty difficulty,
            int[] sequence, int symbolCount, int previewDuration) {

        PuzzleConfig config = new PuzzleConfig(
            "test_ritual_" + difficulty,
            PuzzleId.RITUAL_SEQUENCE,
            difficulty
        );

        RitualSequenceMinigame puzzle =
            new RitualSequenceMinigame(config);

        // Check the selected difficulty settings.
        assertCondition(
            puzzle.getSymbolCount() == symbolCount &&
            puzzle.getSequenceLength() == sequence.length &&
            puzzle.getPreviewDuration() == previewDuration,
            difficulty + ": Incorrect difficulty settings"
        );

        assertCondition(!puzzle.isSolved(),
            difficulty + ": Puzzle starts solved");

        // Enter the first correct symbol.
        puzzle.selectSymbol(sequence[0]);

        assertCondition(puzzle.getCurrentStep() == 1,
            difficulty + ": Correct symbol not accepted");

        // Enter an incorrect symbol to reset progress.
        int wrongSymbol = (sequence[1] + 1) % symbolCount;
        puzzle.selectSymbol(wrongSymbol);

        assertCondition(puzzle.getCurrentStep() == 0,
            difficulty + ": Incorrect symbol did not reset progress");

        assertCondition(puzzle.hasFailed(),
            difficulty + ": Failed attempt not recorded");

        // Enter the entire correct sequence.
        for (int symbol : sequence) {
            puzzle.selectSymbol(symbol);
        }

        assertCondition(puzzle.isSolved(),
            difficulty + ": Correct sequence did not solve puzzle");

        // Completed puzzles must ignore further selections.
        int completedStep = puzzle.getCurrentStep();
        puzzle.selectSymbol(0);

        assertCondition(puzzle.getCurrentStep() == completedStep,
            difficulty + ": Completed puzzle accepted input");

        // Reset the puzzle for a new attempt.
        puzzle.reset();

        assertCondition(!puzzle.isSolved() &&
                        !puzzle.hasFailed() &&
                        puzzle.getCurrentStep() == 0,
            difficulty + ": Reset failed");

        System.out.println(difficulty + ": PASSED");
    }

    // Stop testing immediately when a condition fails.
    private static void assertCondition(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
