
package Puzzles;

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
        267, 333, 400, 467, 533
    };

    private static final int SYMBOL_Y = 310;
    private static final int SYMBOL_RADIUS = 28;

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

        generateSequence(config);
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

        // Match glow and click regions to the artwork.
        for (int i = 0; i < 5; i++) {

            int x = SYMBOL_X[i];
            int y = SYMBOL_Y;

            if (i >= symbolCount) {

                g.setColor(INACTIVE);
                g.fillOval(
                    x - SYMBOL_RADIUS,
                    y - SYMBOL_RADIUS,
                    SYMBOL_RADIUS * 2,
                    SYMBOL_RADIUS * 2
                );

            } else if (i == glowingSymbol) {

                g.setColor(new Color(255, 190, 75, 80));
                g.fillOval(
                    x - SYMBOL_RADIUS,
                    y - SYMBOL_RADIUS,
                    SYMBOL_RADIUS * 2,
                    SYMBOL_RADIUS * 2
                );

                g.setColor(GOLD);
                g.setStroke(new BasicStroke(4));
                g.drawOval(
                    x - SYMBOL_RADIUS,
                    y - SYMBOL_RADIUS -6,
                    SYMBOL_RADIUS * 2,
                    SYMBOL_RADIUS * 2
                );
            }
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

    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved || previewing) {
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
