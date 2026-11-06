package Scripts;

import Level.Script;
import ScriptActions.LockPlayerScriptAction;
import ScriptActions.ScriptAction;
import ScriptActions.TextboxScriptAction;
import ScriptActions.UnlockPlayerScriptAction;

import java.util.ArrayList;

// Plays when the Necromancer wakes: the full exchange the first time, a single taunt after the player has died to it.
// The dialogue lives in the arrays below so the story can be edited in one place.
public class BossIntroScript extends Script {

    public static final String[] FIRST_MEETING_LINES = {
        "(The candles flare a sickly red.\nSomething rises from the dais.)",
        "NECROMANCER: So... the Tenant's little guest\nfound his way to the bottom of the house.",
        "NECROMANCER: You opened the mirror's eye, boy.\nNow everything down here can see you.",
        "DEAN: Then let it watch.\nI'm walking out of here.",
        "NECROMANCER: Servants... rise!",
        "(The door slams shut behind you.)"
    };

    public static final String[] RETURN_LINES = {
        "NECROMANCER: Back again?\nThe dead are patient. I am not."
    };

    private final boolean firstMeeting;

    public BossIntroScript(boolean firstMeeting) {
        this.firstMeeting = firstMeeting;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> actions = new ArrayList<>();
        actions.add(new LockPlayerScriptAction());
        actions.add(new TextboxScriptAction(firstMeeting ? FIRST_MEETING_LINES : RETURN_LINES));
        actions.add(new UnlockPlayerScriptAction());
        return actions;
    }
}
