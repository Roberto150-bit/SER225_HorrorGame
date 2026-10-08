
package Puzzles;

public class PuzzleController {

    private final PuzzleConfig config;
    private final PuzzleMinigame minigame;
    private boolean resultTaken = false;

    private boolean completed = false;

    // Connects one puzzle configuration to its minigame.
    public PuzzleController(PuzzleConfig config, PuzzleMinigame minigame) {
        if (config == null || minigame == null) {
            throw new IllegalArgumentException("Invalid puzzle controller");
        }

        this.config = config;
        this.minigame = minigame;
    }

    // Starts a new attempt.
    public void start() {
        resultTaken = false;
        completed = false;
        minigame.reset();
    }
    
    public void update() {
        if (completed) {
            return;
        }

        minigame.update();

        if (minigame.isSolved()) {
            completed = true;
        }
    }

    // Returns the completion result only once per attempt.
    public PuzzleResult takeResult() {
        if (!completed || resultTaken) {
            return null;
        }

        resultTaken = true;
        return new PuzzleResult(config, true);
    }


    // Returns the minigame for rendering.
    public PuzzleMinigame getMinigame() {
        return minigame;
    }

    public void onClick(int mouseX, int mouseY) {

        // Ignore clicks after the puzzle is solved.
        if (completed) {
            return;
        }

        minigame.onClick(mouseX, mouseY);
    }

}
