package Scripts;

import Level.Script;
import ScriptActions.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


// script for the note object -- shows the note's message in a textbox when the player interacts with it
public class NoteScript extends Script {

    private BufferedImage itemImage;

    public NoteScript(BufferedImage itemImage) {
        this.itemImage = itemImage;
    }
    
    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new LockPlayerScriptAction()); // Locks player until they hit spacebar
        scriptActions.add(new PickupScriptAction(itemImage)); // Item disappears, appears in hotbar if possible
        scriptActions.add(new TextboxScriptAction("You found a note!")); // WILL BE REPLACED WITH THE OPENING OF THE BOOK LATER (+ sound)
        scriptActions.add(new UnlockPlayerScriptAction()); // Unlocks player movement
        return scriptActions;
    }
}