
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class HauntedClockMinigame implements PuzzleMinigame {

    private final int targetHour;
    private final int targetMinute;
    private final int minuteStep;

    private int currentHour;
    private int currentMinute;
    private boolean solved;

    // Configure the clock based on difficulty.
    public HauntedClockMinigame(PuzzleConfig config) {

        switch (config.getDifficulty()) {
            case EASY:
                targetHour = 3;
                targetMinute = 0;
                minuteStep = 15;
                break;

            case MEDIUM:
                targetHour = 6;
                targetMinute = 30;
                minuteStep = 10;
                break;

            case HARD:
                targetHour = 11;
                targetMinute = 45;
                minuteStep = 5;
                break;

            default:
                throw new IllegalArgumentException("Unknown difficulty");
        }

        reset();
    }

    // Move the hour hand forward or backward.
    public void adjustHour(int direction) {
        if (solved) {
            return;
        }

        currentHour = ((currentHour - 1 + direction + 12) % 12) + 1;
        checkSolution();
    }

    // Move the minute hand forward or backward.
    public void adjustMinute(int direction) {
        if (solved) {
            return;
        }

        currentMinute =
            (currentMinute + direction * minuteStep + 60) % 60;

        checkSolution();
    }

    // Check whether both clock hands match the target.
    private void checkSolution() {
        if (currentHour == targetHour &&
            currentMinute == targetMinute) {

            solved = true;
        }
    }

    @Override
    public void update() {
        // No automatic updates required.
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Puzzle title and target time.
        graphicsHandler.drawString(
            "HAUNTED CLOCK",
            290, 185,
            new Font("Serif", Font.BOLD, 28),
            Color.WHITE
        );

        graphicsHandler.drawString(
            String.format("%d:%02d", currentHour, currentMinute),
            350, 255,
            new Font("Serif", Font.BOLD, 38),
            new Color(230, 190, 130)
        );

        graphicsHandler.drawString(
            String.format("Set time to %d:%02d", targetHour, targetMinute),
            305, 300,
            new Font("Arial", Font.PLAIN, 18),
            Color.LIGHT_GRAY
        );

        // Hour adjustment buttons.
        drawButton(graphicsHandler, 245, 325, "H-");
        drawButton(graphicsHandler, 335, 325, "H+");

        // Minute adjustment buttons.
        drawButton(graphicsHandler, 425, 325, "M-");
        drawButton(graphicsHandler, 515, 325, "M+");

        // Show success when the correct time is reached.
        if (solved) {
            graphicsHandler.drawString(
                "TIME RESTORED",
                310, 415,
                new Font("Serif", Font.BOLD, 22),
                Color.GREEN
            );
        }
    }

    // Draw a reusable adjustment button.
    private void drawButton(
            GraphicsHandler graphicsHandler,
            int x, int y, String label) {

        graphicsHandler.drawFilledRectangle(
            x, y, 70, 45, new Color(75, 55, 60)
        );

        graphicsHandler.drawString(
            label,
            x + 15, y + 30,
            new Font("Arial", Font.BOLD, 20),
            Color.WHITE
        );
    }


    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) {
            return;
        }

        // Hour backward.
        if (insideButton(mouseX, mouseY, 245, 325)) {
            adjustHour(-1);
        }

        // Hour forward.
        else if (insideButton(mouseX, mouseY, 335, 325)) {
            adjustHour(1);
        }

        // Minute backward.
        else if (insideButton(mouseX, mouseY, 425, 325)) {
            adjustMinute(-1);
        }

        // Minute forward.
        else if (insideButton(mouseX, mouseY, 515, 325)) {
            adjustMinute(1);
        }
    }

    // Check whether the mouse clicked a specific button.
    private boolean insideButton(
            int mouseX, int mouseY, int x, int y) {

        return mouseX >= x && mouseX <= x + 70 &&
            mouseY >= y && mouseY <= y + 45;
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        currentHour = 12;
        currentMinute = 0;
        solved = false;
    }

    public int getCurrentHour() {
        return currentHour;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }

    public int getTargetHour() {
        return targetHour;
    }

    public int getTargetMinute() {
        return targetMinute;
    }
}
