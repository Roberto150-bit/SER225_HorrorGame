package ScriptActions;

import Level.ScriptState;
import java.awt.image.BufferedImage;

//When player clicks button, item is picked up and added to inventory
//Class is used in NoteScript to pick up the note and add it to the inventory

public class PickupScriptAction extends ScriptAction {

    private BufferedImage itemImage;


    public PickupScriptAction(BufferedImage itemImage) {
        this.itemImage = itemImage;
    }

    @Override
    public ScriptState execute() { // -----------------------------------------------------------------
        //boolean pickedUp = map.getHotbarUI().addItem(itemImage);

        if (entity != null) {
            entity.setIsHidden(true);
            entity.setIsUncollidable(true);
            entity.markCollected(); // stays gone when the player comes back to this map
            System.out.println("isHidden now: " + entity.isHidden());                
            map.getUI().IncreaseNumPages();
            map.setBookOpen(true);
        }


        return ScriptState.COMPLETED;
    }
    
}