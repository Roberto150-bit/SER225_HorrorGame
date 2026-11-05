package Scripts.DungeonMap;

import Level.Script;
import ScriptActions.*;
import java.util.ArrayList;

public class BossEnterScript extends Script {

    private int numberOfTimesEntered = 0;

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> actions = new ArrayList<>();

        actions.add(new ConditionalScriptAction() {{
            addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                addRequirement(new FlagRequirement("HasEnteredRoom", false));

                // Set the flag first so it can't re-trigger
                addScriptAction(new ChangeFlagScriptAction("HasEnteredRoom", true));

                addScriptAction(new LockPlayerScriptAction());

                addScriptAction(new TextboxScriptAction(new String[] {
                    "DEAN: Mrs. Vale? Is that you?",
                    "TENANT: She is not here.\nShe is never here when I arrive.",
                    "DEAN: Who are you?",
                    "TENANT: You have been sleeping\nunder my roof, Dean.",
                    "DEAN: I've found her letters.",
                    "DEAN: Where is Mara?",
                    "(The Tenant pauses. The air turns cold.)",
                    "TENANT: Ah. The letters.\nYou read them so eagerly.",
                    "DEAN: What about the letters?",
                    "TENANT: Her handwriting was difficult\nto learn. I practiced for weeks.",
                    "DEAN: ...No.",
                    "TENANT: Oh, she did write to you once.\nIt simply was not these.",
                    "DEAN: You're lying.",
                    "TENANT: Perhaps.\nHand me the book and find out.",
                    "DEAN: You'll have to take it\nfrom me.",
                    "TENANT: That was always the plan."
                }));

                addScriptAction(new UnlockPlayerScriptAction());
            }});
        }});

        return actions;
    }
}