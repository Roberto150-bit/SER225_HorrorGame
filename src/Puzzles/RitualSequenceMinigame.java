package Puzzles;
//hint done
import Engine.GraphicsHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Random;

public class RitualSequenceMinigame implements PuzzleMinigame {

    private final int symbolCount;
    private final int[] sequence;
    private final int previewDuration;

    private int currentStep;
    private boolean solved;
    private boolean failed;

    private long previewStartTime;
    private boolean previewing;

    // Positions match the five illustrated symbols.
    private static final int[] SYMBOL_X = {
        283, 341, 400, 459, 517
    };

    private static final int SYMBOL_Y = 308;
    private static final int SYMBOL_RADIUS = 23;

    // Hint plaque (bottom-left "?"). Same values as the other puzzles;
    // recalibrate if this panel's artwork sits in a different spot.
    private static final int HINT_X = 110;
    private static final int HINT_Y = 365;
    private static final int HINT_W = 121;
    private static final int HINT_H = 80;

    private static final Color GOLD =
        new Color(245, 195, 110);

    private static final Color INACTIVE =
        new Color(18, 12, 17, 190);

    public RitualSequenceMinigame(PuzzleConfig config) {

        switch (config.getDifficulty()) {
            case EASY:
                symbolCount = 3;
                previewDuration = 1200;
                break;

            case MEDIUM:
                symbolCount = 4;
                previewDuration = 900;
                break;

            case HARD:
                symbolCount = 5;
                previewDuration = 600;
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown difficulty"
                );
        }

        sequence = new int[symbolCount];

        // Start with each symbol exactly once.
        for (int i = 0; i < symbolCount; i++) {
            sequence[i] = i;
        }

        // Shuffle the ritual order for this puzzle instance.
        Random random = new Random();

        for (int i = sequence.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            int temp = sequence[i];
            sequence[i] = sequence[j];
            sequence[j] = temp;
        }

        reset();
    }

    // Generate one stable, shuffled sequence per instance.
    private void generateSequence(PuzzleConfig config) {

        long seed =
            31L * config.getInstanceId().hashCode()
            + config.getPuzzleType().ordinal();

        seed = 31L * seed
            + config.getDifficulty().ordinal();

        Random random = new Random(seed);

        for (int i = 0; i < symbolCount; i++) {
            sequence[i] = i;
        }

        // Fisher-Yates shuffle.
        for (int i = symbolCount - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            int temp = sequence[i];
            sequence[i] = sequence[j];
            sequence[j] = temp;
        }
    }

    public void selectSymbol(int symbolIndex) {

        if (solved || previewing ||
            symbolIndex < 0 ||
            symbolIndex >= symbolCount) {
            return;
        }

        if (failed) {
            failed = false;
        }

        if (sequence[currentStep] == symbolIndex) {
            currentStep++;

            if (currentStep == sequence.length) {
                solved = true;
            }

        } else {
            failed = true;
            currentStep = 0;
        }
    }

    // Hint: replay the glowing sequence without losing progress.
    private void replaySequence() {

        if (solved || previewing) {
            return;
        }

        failed = false;
        previewStartTime = System.currentTimeMillis();
        previewing = true;
    }

    @Override
    public void update() {

        if (!previewing) {
            return;
        }

        long elapsed =
            System.currentTimeMillis() - previewStartTime;

        long totalDuration =
            (long) sequence.length *
            (previewDuration + 300);

        if (elapsed >= totalDuration) {
            previewing = false;
            failed = false;
        }
    }

    private int getGlowingSymbol() {

        if (!previewing) {
            return -1;
        }

        long elapsed =
            System.currentTimeMillis() - previewStartTime;

        int cycle = previewDuration + 300;
        int step = (int)(elapsed / cycle);

        if (step >= sequence.length) {
            return -1;
        }

        if (elapsed % cycle >= previewDuration) {
            return -1;
        }

        return sequence[step];
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        if (solved) {
            return;
        }

        Graphics2D g = graphicsHandler.getGraphics();

        int glowingSymbol = getGlowingSymbol();

        Stroke oldStroke = g.getStroke();

        // Only highlight the active symbol.
        // The background already draws all five gold buttons.
        if (glowingSymbol >= 0 && glowingSymbol < symbolCount) {

            int x = SYMBOL_X[glowingSymbol];
            int y = SYMBOL_Y;

            g.setColor(new Color(255, 210, 100, 65));
            g.fillOval(
                x - 18,
                y - 18,
                36,
                36
            );

            g.setColor(new Color(255, 220, 130, 170));
            g.setStroke(new BasicStroke(2));

            g.drawOval(
                x - 21,
                y - 21,
                42,
                42
            );
        }

        g.setStroke(oldStroke);
        
        // Status text inside the lower plaque.
        String statusText;

        if (previewing) {
            statusText = "MEMORIZE";
        } else if (failed) {
            statusText = "WRONG ORDER";
        } else {
            statusText = "SEQUENCE " +
                currentStep + "/" + sequence.length;
        }

        Font statusFont = new Font(
            "Serif", Font.BOLD, 12
        );

        g.setFont(statusFont);

        int statusWidth =
            g.getFontMetrics().stringWidth(statusText);

        graphicsHandler.drawString(
            statusText,
            400 - statusWidth / 2,
            389,
            statusFont,
            new Color(245, 210, 155)
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

        // The hint is checked before the previewing guard,
        // so clicking it during a replay is simply ignored.
        if (isHintClick(mouseX, mouseY)) {
            replaySequence();
            return;
        }

        if (previewing) {
            return;
        }

        for (int i = 0; i < symbolCount; i++) {

            int dx = mouseX - SYMBOL_X[i];
            int dy = mouseY - SYMBOL_Y;

            if (dx * dx + dy * dy <=
                SYMBOL_RADIUS * SYMBOL_RADIUS) {

                selectSymbol(i);
                return;
            }
        }
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {

        currentStep = 0;
        solved = false;
        failed = false;

        previewStartTime = System.currentTimeMillis();
        previewing = true;
    }

    public int getSymbolCount() {
        return symbolCount;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getSequenceLength() {
        return sequence.length;
    }

    public int getPreviewDuration() {
        return previewDuration;
    }

    public boolean hasFailed() {
        return failed;
    }
}