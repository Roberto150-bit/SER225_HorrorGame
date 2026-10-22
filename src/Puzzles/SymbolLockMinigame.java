
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

    private static final int PANEL_X = 150;
    private static final int PANEL_Y = 100;
    private static final int PANEL_WIDTH = 500;

    // Center the controls inside the artwork's rings.
    private static final int DIAL_Y = 280;
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

    private void generateSolution() {

        long seed = 31L * config.getInstanceId().hashCode()
            + config.getPuzzleType().ordinal();

        seed = 31L * seed
            + config.getDifficulty().ordinal();

        Random random = new Random(seed);

        for (int i = 0; i < dialCount; i++) {
            targetSymbols[i] = random.nextInt(symbolCount);
        }

        // Prevent the puzzle from starting solved.
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

    private void generateClues() {

        int shift = getClueShift();

        for (int i = 0; i < dialCount; i++) {

            int encoded =
                (targetSymbols[i] + shift) % symbolCount;

            if (config.getDifficulty() ==
                    PuzzleDifficulty.HARD) {

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

        if (solved) {
            return;
        }

        java.awt.Graphics2D g = graphicsHandler.getGraphics();

        Color gold = new Color(215, 177, 108);
        Color dark = new Color(38, 26, 35);
        Color inner = new Color(76, 48, 54);
        Color light = new Color(245, 221, 175);

        int centerX = PANEL_X + PANEL_WIDTH / 2;

        // Medium/Hard: hide the three fixed artwork rings
        // with one reusable metal plate for additional dials.
        if (dialCount > 3) {
            graphicsHandler.drawFilledRectangle(
                PANEL_X + 20,
                DIAL_Y - 72,
                PANEL_WIDTH - 40,
                144,
                dark
            );

            graphicsHandler.drawRectangle(
                PANEL_X + 20,
                DIAL_Y - 72,
                PANEL_WIDTH - 40,
                144,
                gold,
                2
            );
        }

        // All difficulties use the same generated dial layout.
        for (int i = 0; i < dialCount; i++) {

            int dialX = getDialCenterX(i);
            int dialY = DIAL_Y;

            int radius = DIAL_RADIUS;

            // Outer brass border.
            g.setColor(gold);
            g.fillOval(
                dialX - radius - 3,
                dialY - radius - 3,
                radius * 2 + 6,
                radius * 2 + 6
            );

            // Circular metal control.
            g.setColor(dark);
            g.fillOval(
                dialX - radius,
                dialY - radius,
                radius * 2,
                radius * 2
            );

            g.setColor(inner);
            g.fillOval(
                dialX - radius + 6,
                dialY - radius + 6,
                radius * 2 - 12,
                radius * 2 - 12
            );

            // Current dial value.
            String value = String.valueOf(
                currentSymbols[i] + 1
            );

            Font dialFont = new Font(
                "Serif", Font.BOLD, 26
            );

            g.setFont(dialFont);

            int textWidth = g.getFontMetrics()
                .stringWidth(value);

            graphicsHandler.drawString(
                value,
                dialX - textWidth / 2,
                dialY + 9,
                dialFont,
                light
            );
        }

        // Dark backing improves readability on the plaque.
        int plaqueX = PANEL_X + 55;
        int plaqueY = PANEL_Y + 251;
        int plaqueWidth = PANEL_WIDTH - 110;

        graphicsHandler.drawFilledRectangle(
            plaqueX,
            plaqueY,
            plaqueWidth,
            43,
            dark
        );

        graphicsHandler.drawRectangle(
            plaqueX,
            plaqueY,
            plaqueWidth,
            43,
            gold,
            1
        );

        // Display the generated clue.
        StringBuilder clue = new StringBuilder();

        for (int i = 0; i < dialCount; i++) {
            if (i > 0) {
                clue.append("  -  ");
            }
            clue.append(clueSymbols[i] + 1);
        }

        Font clueFont = new Font(
            "Serif", Font.BOLD, 17
        );

        g.setFont(clueFont);

        String clueText = "CLUE: " + clue;

        int clueWidth = g.getFontMetrics()
            .stringWidth(clueText);

        graphicsHandler.drawString(
            clueText,
            centerX - clueWidth / 2,
            plaqueY + 18,
            clueFont,
            light
        );

        // Short difficulty instructions.
        String instruction;

        switch (config.getDifficulty()) {
            case EASY:
                instruction = "Enter the symbols in order";
                break;

            case MEDIUM:
                instruction = "Subtract 1 from each - wrap at 5";
                break;

            case HARD:
                instruction = "Reverse order - subtract 2 - wrap at 6";
                break;

            default:
                instruction = "";
        }

        Font hintFont = new Font(
            "Serif", Font.PLAIN, 13
        );

        g.setFont(hintFont);

        int hintWidth = g.getFontMetrics()
            .stringWidth(instruction);

        graphicsHandler.drawString(
            instruction,
            centerX - hintWidth / 2,
            plaqueY + 35,
            hintFont,
            light
        );
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

        // Keep the same solution when restarting.
        Arrays.fill(currentSymbols, 0);
    }
}
