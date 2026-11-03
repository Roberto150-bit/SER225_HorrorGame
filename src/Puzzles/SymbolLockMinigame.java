package Puzzles;
//Hint added
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
    private boolean hintVisible;

    private static final int PANEL_X = 150;
    private static final int PANEL_Y = 100;
    private static final int PANEL_WIDTH = 500;

    // Center the controls inside the artwork's rings.
    private static final int DIAL_Y = 280;
    private static final int DIAL_RADIUS = 30;

    // Hint plaque (bottom-left "?"). Same values as the other puzzles;
    // recalibrate if this panel's artwork sits in a different spot.
    private static final int HINT_X = 110;
    private static final int HINT_Y = 365;
    private static final int HINT_W = 121;
    private static final int HINT_H = 80;

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

        Random random = new Random();

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
                return symbolCount - 1;

            case MEDIUM:
                return 1;

            case HARD:
                return 2;

            default:
                return 0;
        }
    }

    private int getDialCenterX(int index) {
        // Five fixed slots illustrated in the PNG.
        int[] slots = {225, 315, 400, 485, 575};

        switch (dialCount) {
            case 3:
                return slots[new int[]{0, 2, 4}[index]];

            case 4:
                return slots[new int[]{0, 1, 3, 4}[index]];

            case 5:
                return slots[index];

            default:
                throw new IllegalStateException(
                    "Unsupported dial count: " + dialCount
                );
        }
    }

    // Number of dials already showing the right symbol.
    private int countCorrectDials() {

        int count = 0;

        for (int i = 0; i < dialCount; i++) {
            if (currentSymbols[i] == targetSymbols[i]) {
                count++;
            }
        }

        return count;
    }

    // Index of the first wrong dial, or -1 if all are right.
    private int getFirstWrongDial() {

        for (int i = 0; i < dialCount; i++) {
            if (currentSymbols[i] != targetSymbols[i]) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void update() {
        // No continuous updates required.
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        java.awt.Graphics2D g = graphicsHandler.getGraphics();

        Font numberFont = new Font("Serif", Font.BOLD, 24);
        Font clueFont = new Font("Serif", Font.BOLD, 16);
        Font instructionFont = new Font("Serif", Font.PLAIN, 12);

        // The background PNG already contains the five dial
        // frames. Only draw the values of active dials.
        for (int i = 0; i < dialCount; i++) {

            int centerX = getDialCenterX(i);

            String number = String.valueOf(
                currentSymbols[i] + 1
            );

            g.setFont(numberFont);

            int textWidth =
                g.getFontMetrics().stringWidth(number);

            graphicsHandler.drawString(
                number,
                centerX - textWidth / 2,
                DIAL_Y + 9,
                numberFont,
                new Color(250, 220, 165)
            );
        }

        // Hint: ring the first wrong dial and show how many are right.
        if (hintVisible && !solved) {

            int wrongDial = getFirstWrongDial();

            if (wrongDial >= 0) {

                java.awt.Stroke previousStroke = g.getStroke();

                g.setColor(new Color(255, 225, 150));
                g.setStroke(new java.awt.BasicStroke(3f));

                g.drawOval(
                    getDialCenterX(wrongDial) - DIAL_RADIUS,
                    DIAL_Y - DIAL_RADIUS,
                    DIAL_RADIUS * 2,
                    DIAL_RADIUS * 2
                );

                g.setStroke(previousStroke);
            }

            String hint = countCorrectDials() + " OF "
                + dialCount + " DIALS CORRECT";

            Font hintFont = new Font("Serif", Font.ITALIC, 13);

            g.setFont(hintFont);

            int hintWidth =
                g.getFontMetrics().stringWidth(hint);

            graphicsHandler.drawString(
                hint,
                400 - hintWidth / 2,
                DIAL_Y + DIAL_RADIUS + 25,
                hintFont,
                new Color(255, 225, 175)
            );
        }

        // Build the clue from the generated combination.
        StringBuilder clueBuilder = new StringBuilder("CLUE: ");

        for (int i = 0; i < dialCount; i++) {

            if (i > 0) {
                clueBuilder.append(" - ");
            }

            clueBuilder.append(clueSymbols[i] + 1);
        }

        String clueText = clueBuilder.toString();

        String instruction;

        switch (config.getDifficulty()) {
            case EASY:
                instruction = "Add 1 to each - cycle 1-4";
                break;

            case MEDIUM:
                instruction = "Subtract 1 from each - cycle 1-5";
                break;

            case HARD:
                instruction = "Reverse order - subtract 2 - cycle 1-6";
                break;

            default:
                instruction = "";
        }

        // Center the clue inside the bottom gold plaque.
        g.setFont(clueFont);

        int clueWidth =
            g.getFontMetrics().stringWidth(clueText);

        graphicsHandler.drawString(
            clueText,
            400 - clueWidth / 2,
            382,
            clueFont,
            new Color(255, 225, 175)
        );

        // Difficulty instruction underneath the clue.
        g.setFont(instructionFont);

        int instructionWidth =
            g.getFontMetrics().stringWidth(instruction);

        graphicsHandler.drawString(
            instruction,
            400 - instructionWidth / 2,
            393,
            instructionFont,
            new Color(245, 210, 160)
        );
    }

    // The caller can use this to let clicks on the plaque
    // through its insidePuzzle filter.
    public boolean isHintClick(int mouseX, int mouseY) {
        return mouseX >= HINT_X &&
               mouseX <= HINT_X + HINT_W &&
               mouseY >= HINT_Y &&
               mouseY <= HINT_Y + HINT_H;
    }

    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) {
            return;
        }

        // Check the hint plaque first.
        if (isHintClick(mouseX, mouseY)) {
            hintVisible = !hintVisible;
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
        hintVisible = false;

        // Keep the same solution when restarting.
        Arrays.fill(currentSymbols, 0);
    }
}