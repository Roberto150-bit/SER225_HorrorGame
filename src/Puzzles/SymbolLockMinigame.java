
package Puzzles;

import Engine.GraphicsHandler;

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
        // Puzzle artwork and dials will be drawn here.
    }

    @Override
    public void onClick(int mouseX, int mouseY) {
        // Mouse interactions will be added here.
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