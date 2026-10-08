
package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class SymbolLockMinigame implements PuzzleMinigame {

    private final PuzzleConfig config;
    private final int dialCount;
    private final int symbolCount;

    private int[] currentSymbols;
    private int[] targetSymbols;
    private boolean solved;

    // Creates a Symbol Lock with settings based on difficulty.
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
                throw new IllegalArgumentException("Unknown difficulty");
        }

        currentSymbols = new int[dialCount];
        targetSymbols = new int[dialCount];

        reset();
    }

    @Override
    public void update() {
        // Puzzle logic will be added in the next steps.
    }

    
    
    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Match the existing PuzzleUI window position.
        int puzzleX = 150;
        int puzzleY = 100;

        // Dial centers relative to the 500x350 background.
        int[] dialCentersX = {115, 250, 385};
        int dialCenterY = 175;

        // Draw each symbol in its corresponding dial.
        for (int i = 0; i < dialCount; i++) {

            int dialX = puzzleX + dialCentersX[i];
            int dialY = puzzleY + dialCenterY;

            String symbol = String.valueOf(currentSymbols[i] + 1);

            graphicsHandler.drawString(
                symbol,
                dialX - 10,
                dialY + 12,
                new Font("Serif", Font.BOLD, 36),
                new Color(230, 190, 130)
            );
        }

        if (solved) {
            graphicsHandler.drawString(
                "LOCK UNSEALED",
                325,
                465,
                new Font("Serif", Font.BOLD, 20),
                new Color(130, 230, 160)
            );
        }
    }
    
    @Override
    public void onClick(int mouseX, int mouseY) {

        // Match the puzzle window and dial positions.
        int puzzleX = 150;
        int puzzleY = 100;

        int[] dialCentersX = {115, 250, 385};
        int dialCenterY = 175;
        int dialRadius = 55;

        // Find which dial was clicked.
        for (int i = 0; i < dialCount; i++) {

            int centerX = puzzleX + dialCentersX[i];
            int centerY = puzzleY + dialCenterY;

            int dx = mouseX - centerX;
            int dy = mouseY - centerY;

            // Accept clicks inside the circular dial.
            if (dx * dx + dy * dy <= dialRadius * dialRadius) {
                rotateDial(i);
                return;
            }
        }
    }
    
    // Rotates one dial to its next available symbol.
    public void rotateDial(int dialIndex) {
        if (solved || dialIndex < 0 || dialIndex >= dialCount) {
            return;
        }

        currentSymbols[dialIndex] =
            (currentSymbols[dialIndex] + 1) % symbolCount;

        checkSolution();
    }

    // Checks whether every dial matches the target combination.
    private void checkSolution() {
        for (int i = 0; i < dialCount; i++) {
            if (currentSymbols[i] != targetSymbols[i]) {
                return;
            }
        }

        solved = true;
    }


    @Override
    public boolean isSolved() {
        return solved;
    }

    
    @Override
    public void reset() {
        solved = false;

        // Define the correct combination based on difficulty.
        switch (config.getDifficulty()) {
            case EASY:
                targetSymbols = new int[]{1, 2, 3};
                break;

            case MEDIUM:
                targetSymbols = new int[]{2, 4, 1, 3};
                break;

            case HARD:
                targetSymbols = new int[]{5, 2, 4, 1, 3};
                break;

            default:
                throw new IllegalArgumentException("Unknown difficulty");
        }

        // Start every dial at its first symbol.
        for (int i = 0; i < dialCount; i++) {
            currentSymbols[i] = 0;
        }
    }
}