
package Puzzles;

public class TuneRadioLogicTest {

    public static void main(String[] args) {

        testRadio(PuzzleDifficulty.EASY, 80, 90, 100, 2);
        testRadio(PuzzleDifficulty.MEDIUM, 80, 97, 110, 1);
        testRadio(PuzzleDifficulty.HARD, 80, 113, 120, 1);

        System.out.println("All Tune Radio tests passed!");
    }

    // Tests tuning, frequency limits, and completion.
    private static void testRadio(
            PuzzleDifficulty difficulty,
            int min, int target, int max, int step) {

        PuzzleConfig config = new PuzzleConfig(
            "test_radio_" + difficulty,
            PuzzleId.TUNE_RADIO,
            difficulty
        );

        TuneRadioMinigame radio = new TuneRadioMinigame(config);

        // A new radio starts at its minimum frequency.
        assertCondition(radio.getCurrentFrequency() == min,
            difficulty + ": Incorrect starting frequency");

        assertCondition(!radio.isSolved(),
            difficulty + ": Radio starts solved");

        // Tuning downward cannot go below the minimum.
        radio.tune(-1);

        assertCondition(radio.getCurrentFrequency() == min,
            difficulty + ": Frequency went below minimum");

        // Tune upward until the target transmission is found.
        int numberOfClicks = (target - min) / step;

        for (int i = 0; i < numberOfClicks; i++) {
            radio.tune(1);
        }

        assertCondition(radio.getCurrentFrequency() == target,
            difficulty + ": Incorrect target frequency");

        assertCondition(radio.isSolved(),
            difficulty + ": Correct frequency did not solve puzzle");

        // A solved radio should ignore additional tuning.
        radio.tune(1);

        assertCondition(radio.getCurrentFrequency() == target,
            difficulty + ": Solved radio changed frequency");

        // Reset should allow a new attempt.
        radio.reset();

        assertCondition(!radio.isSolved(),
            difficulty + ": Reset failed");

        assertCondition(radio.getCurrentFrequency() == min,
            difficulty + ": Incorrect frequency after reset");

        System.out.println(difficulty + ": PASSED");
    }

    // Stops execution if a test fails.
    private static void assertCondition(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
