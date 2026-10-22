
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class TuneRadioMinigame implements PuzzleMinigame {

    private final int minFrequency = 80;
    private final int maxFrequency;
    private final int tuningStep;
    private final int targetFrequency;

    private int currentFrequency;
    private boolean solved;
    private boolean incorrectAttempt;

    // Illustrated knob positions.
    private static final int LEFT_KNOB_X = 310;
    private static final int RIGHT_KNOB_X = 470;
    private static final int KNOB_Y = 320;
    private static final int KNOB_RADIUS = 35;

    // Entire bottom plaque acts as Submit.
    private static final int SUBMIT_X = 240;
    private static final int SUBMIT_Y = 340;
    private static final int SUBMIT_WIDTH = 320;
    private static final int SUBMIT_HEIGHT = 45;

    private static final Color GOLD =
        new Color(245, 215, 160);

    private static final Color DARK_TEXT =
        new Color(60, 28, 34);

    public TuneRadioMinigame(PuzzleConfig config) {

        switch (config.getDifficulty()) {
            case EASY:
                maxFrequency = 100;
                tuningStep = 2;
                break;

            case MEDIUM:
                maxFrequency = 110;
                tuningStep = 1;
                break;

            case HARD:
                maxFrequency = 120;
                tuningStep = 1;
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown difficulty"
                );
        }

        long seed =
            31L * config.getInstanceId().hashCode()
            + config.getPuzzleType().ordinal();

        seed = 31L * seed
            + config.getDifficulty().ordinal();

        Random random = new Random(seed);

        int positions =
            (maxFrequency - minFrequency) / tuningStep;

        targetFrequency = minFrequency
            + (1 + random.nextInt(positions)) * tuningStep;

        reset();
    }

    public void tune(int direction) {

        if (solved) return;

        currentFrequency += direction * tuningStep;

        currentFrequency = Math.max(
            minFrequency,
            Math.min(maxFrequency, currentFrequency)
        );

        incorrectAttempt = false;
    }

    private void submitFrequency() {

        if (solved) return;

        if (currentFrequency == targetFrequency) {
            solved = true;
        } else {
            incorrectAttempt = true;
        }
    }

    private int getSignalStrength() {

        int distance =
            Math.abs(currentFrequency - targetFrequency);

        return Math.max(0, 100 - distance * 10);
    }

    private String getSignalMessage() {

        if (incorrectAttempt) {
            return "INCORRECT FREQUENCY";
        }

        int strength = getSignalStrength();

        if (strength >= 80) {
            return "STRONG TRANSMISSION";
        }

        if (strength >= 50) {
            return "FAINT TRANSMISSION";
        }

        if (strength >= 20) {
            return "SIGNAL INTERFERENCE";
        }

        return "ONLY STATIC";
    }

    @Override
    public void update() {
        // Mouse-controlled puzzle.
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        java.awt.Graphics2D g =
            graphicsHandler.getGraphics();

        int centerX = 400;

        Color gold = new Color(245, 215, 160);
        Color darkInk = new Color(65, 30, 35);

        // 1. Signal hint on the parchment.
        String message = "SIGNAL: " + getSignalMessage();

        Font statusFont =
            new Font("Serif", Font.BOLD, 15);

        g.setFont(statusFont);

        int statusWidth =
            g.getFontMetrics().stringWidth(message);

        graphicsHandler.drawString(
            message,
            centerX - statusWidth / 2,
            178,
            statusFont,
            darkInk
        );

        // 2. Frequency in the radio display.
        String frequency = currentFrequency + " MHz";

        Font frequencyFont =
            new Font("Serif", Font.BOLD, 24);

        g.setFont(frequencyFont);

        int frequencyWidth =
            g.getFontMetrics().stringWidth(frequency);

        graphicsHandler.drawString(
            frequency,
            centerX - frequencyWidth / 2,
            245,
            frequencyFont,
            gold
        );

        /*
        int strength = getSignalStrength();

        graphicsHandler.drawFilledRectangle(
            345,
            264,
            110 * strength / 100,
            5,
            new Color(190, 155, 105)
        );*/

        //
        Font knobFont =
            new Font("Serif", Font.BOLD, 22);

        graphicsHandler.drawString(
            "-",
            LEFT_KNOB_X - 6,
            KNOB_Y + 7,
            knobFont,
            darkInk
        );

        graphicsHandler.drawString(
            "+",
            RIGHT_KNOB_X - 7,
            KNOB_Y + 7,
            knobFont,
            darkInk
        );

        // 5. Submit label on the existing plaque.
        String submitText = incorrectAttempt
            ? "TRY AGAIN - SUBMIT"
            : "SUBMIT FREQUENCY";

        Font submitFont =
            new Font("Serif", Font.BOLD, 18);

        g.setFont(submitFont);

        int submitWidth =
            g.getFontMetrics().stringWidth(submitText);

        graphicsHandler.drawString(
            submitText,
            centerX - submitWidth / 2,
            380,
            submitFont,
            darkInk
        );
    }



    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) return;

        // Check knobs first to avoid hitbox conflicts.
        int leftDx = mouseX - LEFT_KNOB_X;
        int leftDy = mouseY - KNOB_Y;

        if (leftDx * leftDx + leftDy * leftDy
                <= KNOB_RADIUS * KNOB_RADIUS) {

            tune(-1);
            return;
        }

        int rightDx = mouseX - RIGHT_KNOB_X;
        int rightDy = mouseY - KNOB_Y;

        if (rightDx * rightDx + rightDy * rightDy
                <= KNOB_RADIUS * KNOB_RADIUS) {

            tune(1);
            return;
        }

        // Check the entire Submit plaque.
        boolean clickedSubmit =
            mouseX >= SUBMIT_X &&
            mouseX <= SUBMIT_X + SUBMIT_WIDTH &&
            mouseY >= SUBMIT_Y &&
            mouseY <= SUBMIT_Y + SUBMIT_HEIGHT;

        if (clickedSubmit) {
            submitFrequency();
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
        incorrectAttempt = false;
    }

    public int getCurrentFrequency() {
        return currentFrequency;
    }

    public int getTargetFrequency() {
        return targetFrequency;
    }
}
