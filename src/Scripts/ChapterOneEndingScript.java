package Scripts;

import Level.GameListener;
import Level.Script;
import Level.ScriptState;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.TextboxScriptAction;
import ScriptActions.UnlockPlayerScriptAction;

import java.util.ArrayList;

// Plays after the Necromancer is defeated: a few closing lines, then the "Chapter 1 Complete" card.
// The game doesn't end there: the player can carry on from the Necromancer's room.
// The dialogue lives in ENDING_LINES so the story can be edited in one place.
public class ChapterOneEndingScript extends Script {

    public static final String[] ENDING_LINES = {
        "(The Necromancer crumbles to ash.\nThe cold in the room finally lifts.)",
        "DEAN: Whatever you were...\nyou won't hurt anyone else.",
        "(Somewhere above, the floorboards creak.\nThe Tenant is still awake.)",
        "DEAN: Hold on, Mara.\nI'm getting out of here.",
        "(Dean closes his hand around the locket.\nWhatever waits beyond this room, he's ready.)"
    };

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> actions = new ArrayList<>();
        actions.add(new LockPlayerScriptAction());
        actions.add(new TextboxScriptAction(ENDING_LINES));

        // the "Chapter 1 Complete" card comes up; the game waits there until the player picks Continue
        actions.add(new ScriptAction() {
            @Override
            public ScriptState execute() {
                for (GameListener listener : map.getListeners()) {
                    listener.onWin();
                }
                return ScriptState.COMPLETED;
            }
        });
        actions.add(new UnlockPlayerScriptAction());
        return actions;
    }
}
