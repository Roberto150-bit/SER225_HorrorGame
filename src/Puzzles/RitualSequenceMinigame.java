
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class RitualSequenceMinigame implements PuzzleMinigame {

    private final int symbolCount;
    private final int[] sequence;
    private final int previewDuration;

    private int currentStep;
    private boolean solved;
    private boolean failed;

    
    private long previewStartTime;
    private boolean previewing;

    // Configure the sequence based on difficulty.
    public RitualSequenceMinigame(PuzzleConfig config) {

        switch (config.getDifficulty()) {
            case EASY:
                symbolCount = 3;
                sequence = new int[]{0, 2, 1};
                previewDuration = 1200;
                break;

            case MEDIUM:
                symbolCount = 4;
                sequence = new int[]{1, 3, 0, 2};
                previewDuration = 900;
                break;

            case HARD:
                symbolCount = 5;
                sequence = new int[]{2, 4, 1, 3, 0};
                previewDuration = 600;
                break;

            default:
                throw new IllegalArgumentException("Unknown difficulty");
        }

        reset();
    }

    // Process the symbol selected by the player.
    public void selectSymbol(int symbolIndex) {
        if (solved || symbolIndex < 0 || symbolIndex >= symbolCount) {
            return;
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

        // Each symbol glows, followed by a short pause.
        long elapsed = System.currentTimeMillis() - previewStartTime;
        long totalDuration = (long) sequence.length
            * (previewDuration + 300);

        if (elapsed >= totalDuration) {
            previewing = false;
        }
    }
    

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        graphicsHandler.drawString(
            "RITUAL SEQUENCE",
            285, 185,
            new Font("Serif", Font.BOLD, 27),
            Color.WHITE
        );

        // Display instructions based on the current phase.
        String instruction = previewing
            ? "WATCH THE SYMBOLS"
            : "REPEAT THE SEQUENCE";

        graphicsHandler.drawString(
            instruction,
            290, 230,
            new Font("Arial", Font.BOLD, 18),
            Color.LIGHT_GRAY
        );

        // Calculate button positions.
        int buttonSize = 60;
        int spacing = 12;
        int totalWidth = symbolCount * buttonSize
            + (symbolCount - 1) * spacing;
        int startX = 400 - totalWidth / 2;

        // Identify which symbol should glow.
        int glowingSymbol = -1;

        if (previewing) {
            long elapsed = System.currentTimeMillis() - previewStartTime;
            int cycle = previewDuration + 300;
            int step = (int) (elapsed / cycle);

            if (step < sequence.length &&
                elapsed % cycle < previewDuration) {

                glowingSymbol = sequence[step];
            }
        }

        // Draw the available symbols.
        for (int i = 0; i < symbolCount; i++) {

            int buttonX = startX + i * (buttonSize + spacing);

            Color buttonColor = (i == glowingSymbol)
                ? new Color(255, 180, 65)
                : new Color(85, 45, 65);

            graphicsHandler.drawFilledRectangle(
                buttonX, 265, buttonSize, buttonSize,
                buttonColor
            );

            graphicsHandler.drawString(
                String.valueOf(i + 1),
                buttonX + 20, 305,
                new Font("Serif", Font.BOLD, 28),
                Color.WHITE
            );
        }

        // Display preview or player progress.
        if (previewing) {
            graphicsHandler.drawString(
                "Memorize the order...",
                310, 365,
                new Font("Arial", Font.BOLD, 18),
                Color.YELLOW
            );
        } else {
            graphicsHandler.drawString(
                "Progress: " + currentStep + "/" + sequence.length,
                330, 365,
                new Font("Arial", Font.BOLD, 18),
                Color.WHITE
            );
        }

        // Display completion or incorrect sequence.
        if (solved) {
            graphicsHandler.drawString(
                "RITUAL COMPLETE",
                305, 405,
                new Font("Serif", Font.BOLD, 22),
                Color.GREEN
            );
        } else if (failed && !previewing) {
            graphicsHandler.drawString(
                "INCORRECT - TRY AGAIN",
                280, 405,
                new Font("Arial", Font.BOLD, 18),
                Color.RED
            );
        }
    }



    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved || previewing) {
            return;
        }

        // Use the same button positions as draw().
        int buttonSize = 60;
        int spacing = 12;
        int totalWidth = symbolCount * buttonSize
            + (symbolCount - 1) * spacing;
        int startX = 400 - totalWidth / 2;

        for (int i = 0; i < symbolCount; i++) {

            int buttonX = startX + i * (buttonSize + spacing);

            if (mouseX >= buttonX &&
                mouseX <= buttonX + buttonSize &&
                mouseY >= 265 &&
                mouseY <= 265 + buttonSize) {

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

        
        // Automatically begin the preview for each new attempt.
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
