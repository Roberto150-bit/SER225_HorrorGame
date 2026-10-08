
package Puzzles;

public class HauntedClockLogicTest {

    public static void main(String[] args) {

        testClock(PuzzleDifficulty.EASY, 3, 0, 15);
        testClock(PuzzleDifficulty.MEDIUM, 6, 30, 10);
        testClock(PuzzleDifficulty.HARD, 11, 45, 5);

        System.out.println("All Haunted Clock tests passed!");
    }

    // Test one difficulty's controls and completion.
    private static void testClock(
            PuzzleDifficulty difficulty,
            int targetHour, int targetMinute, int minuteStep) {

        PuzzleConfig config = new PuzzleConfig(
            "test_clock_" + difficulty,
            PuzzleId.HAUNTED_CLOCK,
            difficulty
        );

        HauntedClockMinigame clock = new HauntedClockMinigame(config);

        // Every attempt begins at 12:00.
        assertCondition(clock.getCurrentHour() == 12 &&
                        clock.getCurrentMinute() == 0,
            difficulty + ": Incorrect starting time");

        assertCondition(!clock.isSolved(),
            difficulty + ": Clock starts solved");

        // Verify that moving backward from 12 wraps to 11.
        clock.adjustHour(-1);
        assertCondition(clock.getCurrentHour() == 11,
            difficulty + ": Hour wrap failed");

        clock.reset();

        // Set the minute hand before reaching the target hour.
        for (int minute = 0; minute < targetMinute; minute += minuteStep) {
            clock.adjustMinute(1);
        }

        // Move from 12 toward the target hour.
        for (int i = 0; i < targetHour; i++) {
            clock.adjustHour(1);
        }

        assertCondition(clock.getCurrentHour() == targetHour &&
                        clock.getCurrentMinute() == targetMinute,
            difficulty + ": Incorrect target time");

        assertCondition(clock.isSolved(),
            difficulty + ": Correct time did not solve puzzle");

        // A solved puzzle must ignore further changes.
        clock.adjustHour(1);
        clock.adjustMinute(1);

        assertCondition(clock.getCurrentHour() == targetHour &&
                        clock.getCurrentMinute() == targetMinute,
            difficulty + ": Solved clock changed");

        // Reset should clear the completed state.
        clock.reset();

        assertCondition(!clock.isSolved() &&
                        clock.getCurrentHour() == 12 &&
                        clock.getCurrentMinute() == 0,
            difficulty + ": Reset failed");

        System.out.println(difficulty + ": PASSED");
    }

    private static void assertCondition(
            boolean condition, String message) {

        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
