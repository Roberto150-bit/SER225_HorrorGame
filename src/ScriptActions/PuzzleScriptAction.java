
package ScriptActions;

import Level.ScriptState;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleManager;
import Puzzles.PuzzleResult;

public class PuzzleScriptAction extends ScriptAction {

    private final PuzzleConfig config;

    // Receives the configuration from PuzzleScript.
    public PuzzleScriptAction(PuzzleConfig config) {
        this.config = config;
    }

    @Override
    public void setup() {
        // Opens the UI with this object's specific configuration.
        map.getPuzzleUI().open(config);
    }

    
    @Override
    public ScriptState execute() {

        // Check whether the puzzle has produced a completion result.
        PuzzleResult result = map.getPuzzleUI().takeResult();

        if (result != null) {
            PuzzleManager.recordResult(result);
        }

        // Keep the interaction active while the UI is open.
        if (map.getPuzzleUI().isOpen()) {
            return ScriptState.RUNNING;
        }

        return ScriptState.COMPLETED;
    }

}
