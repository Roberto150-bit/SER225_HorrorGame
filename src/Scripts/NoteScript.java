package Scripts;

import Level.Script;
import ScriptActions.*;
import java.util.ArrayList;

// picks up a story note and opens the journal to its page
public class NoteScript extends Script {

    private final int page;

    // page is which journal page (1-5) this note holds
    public NoteScript(int page) {
        this.page = page;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new PickupScriptAction(page)); // note disappears and its page is added to the journal
        return scriptActions;
    }
}
