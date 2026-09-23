package ScriptActions;

import Level.ScriptState;

public class PuzzleScriptAction extends ScriptAction {

    @Override
    public void setup() {
        map.getPuzzleUI().open();
    }

    @Override
    public ScriptState execute() {

        if (map.getPuzzleUI().isOpen()) {
            return ScriptState.RUNNING;
        }

        return ScriptState.COMPLETED;
    }
}