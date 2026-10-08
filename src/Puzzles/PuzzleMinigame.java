
package Puzzles;

import Engine.GraphicsHandler;

public interface PuzzleMinigame {

    // Updates the puzzle's interactions and logic.
    void update();

    // Draws the puzzle inside the game's graphics window.
    void draw(GraphicsHandler graphicsHandler);

    // Handles a mouse click inside the puzzle.
    void onClick(int mouseX, int mouseY);
    
    // Returns true when the puzzle's win condition is met.
    boolean isSolved();

    // Resets the puzzle when a new attempt begins.
    void reset();
}
