
package Puzzles;

public class SymbolLockLogicTest {

    public static void main(String[] args) {

        testDifficulty(PuzzleDifficulty.EASY,
            new int[]{1, 2, 3});

        testDifficulty(PuzzleDifficulty.MEDIUM,
            new int[]{2, 4, 1, 3});

        testDifficulty(PuzzleDifficulty.HARD,
            new int[]{5, 2, 4, 1, 3});

        System.out.println("All Symbol Lock tests passed!");
    }

    // Tests the correct combination for one difficulty.
    private static void testDifficulty(
            PuzzleDifficulty difficulty, int[] target) {

        PuzzleConfig config = new PuzzleConfig(
            "test_" + difficulty,
            PuzzleId.SYMBOL_LOCK,
            difficulty
        );

        SymbolLockMinigame puzzle =
            new SymbolLockMinigame(config);

        // A new puzzle should not already be solved.
        assertCondition(!puzzle.isSolved(),
            difficulty + ": Puzzle starts solved");

        // Rotate each dial to its target position.
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[i]; j++) {
                puzzle.rotateDial(i);
            }
        }

        // The correct combination should solve the puzzle.
        assertCondition(puzzle.isSolved(),
            difficulty + ": Correct combination failed");

        // Reset should make the puzzle unsolved again.
        puzzle.reset();

        assertCondition(!puzzle.isSolved(),
            difficulty + ": Reset failed");

        System.out.println(difficulty + ": PASSED");
    }

    // Stops the test if an expected condition is false.
    private static void assertCondition(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
