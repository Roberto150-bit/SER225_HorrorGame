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
            "MRS. VALE: Easy now, dear.\nYou took a nasty knock.",
            "DEAN: Where... where am I?",
            "MRS. VALE: Somewhere dry.\nI found you on my porch in the storm.",
            "DEAN: My locket. Where's my locket?",
            "MRS. VALE: Here you are.\nYour things are on the table.",
            "(Dean's hand closes around the worn silver locket.\nHe exhales)",
            "DEAN: This is Mara's...",
            "MRS. VALE: Mara... Lovely name.\nYour wife?",
            "DEAN: Girlfriend.\nShe's waiting for me.",
            "MRS. VALE: Then we'll get you well enough\nto go. Drink your tea.",
            "(The floorboards creak slowly above you.)",
            "DEAN: Is someone else here?",
            "MRS. VALE: Just the Tenant.\nKeeps odd hours. Pay it no mind.",
            "DEAN: The door's locked.",
            "MRS. VALE: Where are you in a rush to?\nI'll open it for the morning",
            "MRS. VALE: Things aren't what they seem \n in the night",
            "DEAN: I can't wait until morning.",
            "MRS. VALE: You can barely stand.\nLet an old woman look after you.",
            "DEAN: ...I never told you my name.",
            "MRS. VALE: You said it in your sleep.\nRight next to hers.",
            "MRS. VALE: Use W, A, S, D\nto move around.",
            "MRS. VALE: Press E to interact.\nPress C to open your journal.",
            "MRS. VALE: Here, the Tenant's Book.\nIt may help with your memories.",
            "DEAN: Where is the exit?",
            "MRS. VALE: Patience, dear.\nStart with that note nearby.",
            "MRS. VALE: And Dean...\nDo be careful."
        }));
        actions.add(new UnlockPlayerScriptAction());

        return actions;
    }
}