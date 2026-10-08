
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class TuneRadioMinigame implements PuzzleMinigame {

    private final PuzzleConfig config;

    private final int minFrequency;
    private final int maxFrequency;
    private final int tuningStep;
    private final int targetFrequency;

    private int currentFrequency;
    private boolean solved;

    // Set the radio's tuning difficulty.
    public TuneRadioMinigame(PuzzleConfig config) {
        this.config = config;

        switch (config.getDifficulty()) {
            case EASY:
                minFrequency = 80;
                maxFrequency = 100;
                tuningStep = 2;
                targetFrequency = 90;
                break;

            case MEDIUM:
                minFrequency = 80;
                maxFrequency = 110;
                tuningStep = 1;
                targetFrequency = 97;
                break;

            case HARD:
                minFrequency = 80;
                maxFrequency = 120;
                tuningStep = 1;
                targetFrequency = 113;
                break;

            default:
                throw new IllegalArgumentException("Unknown difficulty");
        }

        reset();
    }

    // Adjust frequency without exceeding its allowed range.
    public void tune(int direction) {
        if (solved) {
            return;
        }

        currentFrequency += direction * tuningStep;

        currentFrequency = Math.max(
            minFrequency,
            Math.min(maxFrequency, currentFrequency)
        );

        // Reaching the target transmission solves the puzzle.
        if (currentFrequency == targetFrequency) {
            solved = true;
        }
    }

    @Override
    public void update() {
        // No automatic updates needed yet.
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        // Display the radio's current frequency.
        graphicsHandler.drawString(
            "TUNE RADIO",
            320, 190,
            new Font("Serif", Font.BOLD, 28),
            Color.WHITE
        );

        graphicsHandler.drawString(
            currentFrequency + " MHz",
            340, 270,
            new Font("Serif", Font.BOLD, 36),
            new Color(230, 190, 130)
        );

        // Show the target frequency for initial testing.
        graphicsHandler.drawString(
            "Find signal: " + targetFrequency + " MHz",
            290, 330,
            new Font("Arial", Font.PLAIN, 18),
            Color.LIGHT_GRAY
        );

        // Display success after reaching the correct frequency.
        if (solved) {
            graphicsHandler.drawString(
                "SIGNAL FOUND",
                320, 380,
                new Font("Serif", Font.BOLD, 24),
                Color.GREEN
            );
        }

        
        // Draw the decrease-frequency button.
        graphicsHandler.drawFilledRectangle(
            270, 345, 80, 45, new Color(75, 55, 60)
        );

        graphicsHandler.drawString(
            "-",
            298, 378,
            new Font("Arial", Font.BOLD, 30),
            Color.WHITE
        );

        // Draw the increase-frequency button.
        graphicsHandler.drawFilledRectangle(
            450, 345, 80, 45, new Color(75, 55, 60)
        );

        graphicsHandler.drawString(
            "+",
            476, 378,
            new Font("Arial", Font.BOLD, 30),
            Color.WHITE
        );

    }


    @Override
    public void onClick(int mouseX, int mouseY) {

        // Ignore clicks once the radio is solved.
        if (solved) {
            return;
        }

        // Decrease frequency when the left button is clicked.
        if (mouseX >= 270 && mouseX <= 350 &&
            mouseY >= 345 && mouseY <= 390) {

            tune(-1);
        }

        // Increase frequency when the right button is clicked.
        if (mouseX >= 450 && mouseX <= 530 &&
            mouseY >= 345 && mouseY <= 390) {

            tune(1);
        }
    }


    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        currentFrequency = minFrequency;
        solved = false;
    }

    // Provides the frequency for the visual display.
    public int getCurrentFrequency() {
        return currentFrequency;
    }

    public int getTargetFrequency() {
        return targetFrequency;
    }
}
