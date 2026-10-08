package Scripts;

import java.util.ArrayList;

import Level.Script;
import Puzzles.PuzzleConfig;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.PuzzleScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.UnlockPlayerScriptAction;

public class PuzzleScript extends Script {

    private final PuzzleConfig config;

    // Each world object provides its own puzzle configuration.
    public PuzzleScript(PuzzleConfig config) {
        this.config = config;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {

        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new LockPlayerScriptAction());
        scriptActions.add(new PuzzleScriptAction(config));
        scriptActions.add(new UnlockPlayerScriptAction());
        return scriptActions;
    }
}