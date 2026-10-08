
package Puzzles;

public class SpiritMirrorLogicTest {

    public static void main(String[] args) {

        testMirror(PuzzleDifficulty.EASY, 3);
        testMirror(PuzzleDifficulty.MEDIUM, 4);
        testMirror(PuzzleDifficulty.HARD, 5);

        System.out.println("All Spirit Mirror tests passed!");
    }

    // Test fragment rotation and puzzle completion.
    private static void testMirror(
            PuzzleDifficulty difficulty, int expectedFragments) {

        PuzzleConfig config = new PuzzleConfig(
            "test_mirror_" + difficulty,
            PuzzleId.SPIRIT_MIRROR,
            difficulty
        );

        SpiritMirrorMinigame mirror =
            new SpiritMirrorMinigame(config);

        // Check difficulty settings and starting state.
        assertCondition(
            mirror.getFragmentCount() == expectedFragments,
            difficulty + ": Incorrect fragment count"
        );

        assertCondition(!mirror.isSolved(),
            difficulty + ": Puzzle starts solved");

        // Check that four rotations return to the starting position.
        mirror.rotateFragment(0);
        mirror.rotateFragment(0);
        mirror.rotateFragment(0);
        mirror.rotateFragment(0);

        assertCondition(mirror.getCurrentRotation(0) == 0,
            difficulty + ": Fragment rotation did not wrap");

        // Rotate each fragment to its target orientation.
        for (int i = 0; i < expectedFragments; i++) {
            int target = mirror.getTargetRotation(i);

            for (int j = 0; j < target; j++) {
                mirror.rotateFragment(i);
            }
        }

        assertCondition(mirror.isSolved(),
            difficulty + ": Correct alignment did not solve puzzle");

        // Solved fragments should no longer rotate.
        int completedRotation = mirror.getCurrentRotation(0);
        mirror.rotateFragment(0);

        assertCondition(
            mirror.getCurrentRotation(0) == completedRotation,
            difficulty + ": Completed fragment changed"
        );

        // Reset returns all fragments to their starting positions.
        mirror.reset();

        assertCondition(!mirror.isSolved(),
            difficulty + ": Reset failed");

        for (int i = 0; i < expectedFragments; i++) {
            assertCondition(mirror.getCurrentRotation(i) == 0,
                difficulty + ": Fragment did not reset");
        }

        System.out.println(difficulty + ": PASSED");
    }

    // Stop immediately if a test fails.
    private static void assertCondition(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
