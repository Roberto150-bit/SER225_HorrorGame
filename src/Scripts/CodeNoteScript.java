package Scripts;

import Engine.SoundPlayer;
import Level.Script;
import Level.ScriptState;
import Puzzles.SealCode;
import ScriptActions.*;
import java.util.ArrayList;

// a note holding one digit of the final seal's code
// picking it up removes it from the map and opens the journal to the page listing every digit found so far
public class CodeNoteScript extends Script {

    private final int digitIndex;

    public CodeNoteScript(int digitIndex) {
        this.digitIndex = digitIndex;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new ScriptAction() {
            @Override
            public ScriptState execute() {
                entity.setIsHidden(true);
                entity.setIsUncollidable(true);
                entity.markCollected(); // stays gone when the player comes back to this map
                SealCode.markFound(digitIndex);
                SoundPlayer.play("NotePickupEffect.wav");
                map.getUI().showCodePage();
                map.setBookOpen(true);
                return ScriptState.COMPLETED;
            }
        });
        return scriptActions;
    }
}
