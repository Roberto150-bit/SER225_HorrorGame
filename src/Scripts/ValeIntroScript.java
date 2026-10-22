package Scripts;

import Level.Script;
import ScriptActions.*;
import java.util.ArrayList;

public class ValeIntroScript extends Script {

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> actions = new ArrayList<>();

        actions.add(new LockPlayerScriptAction());

        actions.add(new TextboxScriptAction(new String[] {
            "(Press SPACE to start conversation)",
            "MRS. VALE: Ah, Dean.\nYou're finally awake.",
            "DEAN: Where am I?\nWho are you?",
            "MRS. VALE: Mrs. Vale, dear.\nWelcome to my home.",
            "DEAN: How did I get here?",
            "MRS. VALE: All in good time.\nI'm sure you have questions.",
            "DEAN: I just want to leave.",
            "MRS. VALE: Of course.\nBut this house is not what it seems.",
            "DEAN: What does that mean?",
            "MRS. VALE: You'll understand soon enough.",
            "MRS. VALE: Use W, A, S, D\nto move around.",
            "MRS. VALE: Press E to interact.\nPress C to open your journal.",
            "MRS. VALE: Here the Tenant's Book. \nIt may help with your memories.",
            "DEAN: Where is the exit?",
            "MRS. VALE: Patience, dear.\nStart with that note nearby.",
            "MRS. VALE: And Dean...\nDo be careful."
        }));

        actions.add(new UnlockPlayerScriptAction());

        return actions;
    }
}