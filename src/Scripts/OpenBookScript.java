package Scripts;

import java.util.ArrayList;
import Level.Script;
import ScriptActions.*;

//for preventing the player from moving while the book is open
public class OpenBookScript extends Script{

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new LockPlayerScriptAction());
        scriptActions.add(new UnlockPlayerScriptAction());


        return scriptActions;
    }
}
