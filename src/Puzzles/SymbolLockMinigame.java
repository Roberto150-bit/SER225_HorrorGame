
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Random;

public class SymbolLockMinigame implements PuzzleMinigame {

    private final PuzzleConfig config;
    private final int dialCount;
    private final int symbolCount;

    private final int[] currentSymbols;
    private final int[] targetSymbols;
    private final int[] clueSymbols;

    private boolean solved;

    // Matches the existing PuzzleUI window.
    private static final int PANEL_X = 150;
    private static final int PANEL_Y = 100;
    private static final int PANEL_WIDTH = 500;

    private static final int DIAL_Y = 310;
    private static final int DIAL_RADIUS = 29;

    private static final Color GOLD =
        new Color(220, 190, 135);

    private static final Color DARK =
        new Color(38, 34, 32);

    public SymbolLockMinigame(PuzzleConfig config) {
        this.config = config;

        switch (config.getDifficulty()) {
            case EASY:
                dialCount = 3;
                symbolCount = 4;
                break;

            case MEDIUM:
                dialCount = 4;
                symbolCount = 5;
                break;

            case HARD:
                dialCount = 5;
                symbolCount = 6;
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown difficulty"
                );
        }

        currentSymbols = new int[dialCount];
        targetSymbols = new int[dialCount];
        clueSymbols = new int[dialCount];

        generateSolution();
        reset();
    }

    // Generate a reproducible combination for this instance.
    private void generateSolution() {

        long seed = 31L * config.getInstanceId().hashCode()
            + config.getPuzzleType().ordinal();

        seed = 31L * seed
            + config.getDifficulty().ordinal();

        Random random = new Random(seed);

        // Every instance receives its own combination.
        for (int i = 0; i < dialCount; i++) {
            targetSymbols[i] = random.nextInt(symbolCount);
        }

        // Avoid starting with an already-solved combination.
        boolean allFirst = true;

        for (int value : targetSymbols) {
            if (value != 0) {
                allFirst = false;
                break;
            }
        }

        if (allFirst) {
            targetSymbols[0] = 1;
        }

        generateClues();
    }

    // Generate clues directly from the correct answer.
    private void generateClues() {

        int shift = getClueShift();

        for (int i = 0; i < dialCount; i++) {

            int encoded =
                (targetSymbols[i] + shift) % symbolCount;

            if (config.getDifficulty() ==
                    PuzzleDifficulty.HARD) {

                // Hard clues are also reversed.
                clueSymbols[dialCount - 1 - i] = encoded;

            } else {
                clueSymbols[i] = encoded;
            }
        }
    }

    private int getClueShift() {

        switch (config.getDifficulty()) {
            case EASY:
                return 0;

            case MEDIUM:
                return 1;

            case HARD:
                return 2;

            default:
                return 0;
        }
    }

    // One shared layout for every difficulty.
    private int getDialCenterX(int index) {

        int spacing = PANEL_WIDTH / (dialCount + 1);

        return PANEL_X + spacing * (index + 1);
    }

    @Override
    public void update() {
        // No continuous updates required.
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        Font heading = new Font(
            "Serif", Font.BOLD, 19
        );

        Font numberFont = new Font(
            "Serif", Font.BOLD, 29
        );

        Font instructionFont = new Font(
            "Serif", Font.PLAIN, 15
        );

        // Dark area for puzzle-specific instructions.
        graphicsHandler.drawFilledRectangle(
            PANEL_X + 20,
            PANEL_Y + 75,
            460,
            95,
            DARK
        );

        graphicsHandler.drawString(
            "THE SEALED COMBINATION",
            PANEL_X + 115,
            PANEL_Y + 98,
            heading,
            GOLD
        );

        String instruction;

        switch (config.getDifficulty()) {

            case EASY:
                instruction =
                    "Enter the etched symbols in order.";
                break;

            case MEDIUM:
                instruction =
                    "Subtract 1 from each symbol. Wrap around.";
                break;

            case HARD:
                instruction =
                    "Read right to left. Subtract 2. Wrap around.";
                break;

            default:
                instruction = "";
        }

        graphicsHandler.drawString(
            instruction,
            PANEL_X + 45,
            PANEL_Y + 119,
            instructionFont,
            Color.LIGHT_GRAY
        );

        // Display clues using the generated solution.
        for (int i = 0; i < dialCount; i++) {

            int centerX = getDialCenterX(i);

            graphicsHandler.drawString(
                String.valueOf(clueSymbols[i] + 1),
                centerX - 9,
                PANEL_Y + 158,
                numberFont,
                GOLD
            );
        }

        graphicsHandler.drawString(
            "ROTATE THE DIALS",
            PANEL_X + 160,
            PANEL_Y + 192,
            instructionFont,
            GOLD
        );

        // Draw one interactive dial per active symbol.
        for (int i = 0; i < dialCount; i++) {

            int centerX = getDialCenterX(i);

            graphicsHandler.drawFilledRectangle(
                centerX - DIAL_RADIUS,
                DIAL_Y - DIAL_RADIUS,
                DIAL_RADIUS * 2,
                DIAL_RADIUS * 2,
                DARK
            );

            graphicsHandler.drawRectangle(
                centerX - DIAL_RADIUS,
                DIAL_Y - DIAL_RADIUS,
                DIAL_RADIUS * 2,
                DIAL_RADIUS * 2,
                GOLD,
                2
            );

            graphicsHandler.drawString(
                String.valueOf(currentSymbols[i] + 1),
                centerX - 9,
                DIAL_Y + 10,
                numberFont,
                Color.WHITE
            );
        }

        graphicsHandler.drawString(
            "Click each dial to rotate",
            PANEL_X + 150,
            PANEL_Y + 255,
            instructionFont,
            Color.LIGHT_GRAY
        );

        if (solved) {
            graphicsHandler.drawString(
                "LOCK UNSEALED",
                PANEL_X + 165,
                PANEL_Y + 325,
                heading,
                new Color(140, 215, 165)
            );
        }
    }

    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) {
            return;
        }

        for (int i = 0; i < dialCount; i++) {

            int centerX = getDialCenterX(i);

            boolean insideDial =
                mouseX >= centerX - DIAL_RADIUS &&
                mouseX <= centerX + DIAL_RADIUS &&
                mouseY >= DIAL_Y - DIAL_RADIUS &&
                mouseY <= DIAL_Y + DIAL_RADIUS;

            if (insideDial) {
                rotateDial(i);
                return;
            }
        }
    }

    public void rotateDial(int dialIndex) {

        if (solved ||
            dialIndex < 0 ||
            dialIndex >= dialCount) {
            return;
        }

        currentSymbols[dialIndex] =
            (currentSymbols[dialIndex] + 1) % symbolCount;

        checkSolution();
    }

    private void checkSolution() {

        if (Arrays.equals(
                currentSymbols, targetSymbols)) {

            solved = true;
        }
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {

        solved = false;

        // Reset player inputs, NOT the generated solution.
        Arrays.fill(currentSymbols, 0);
    }
}
