package ScriptActions;

import Level.ScriptState;
import java.awt.image.BufferedImage;

//When player clicks button, item is picked up and added to inventory

public class PickupScriptAction extends ScriptAction {

    private BufferedImage itemImage;


    public PickupScriptAction(BufferedImage itemImage) {
        this.itemImage = itemImage;
    }

    @Override
    public ScriptState execute() { // -----------------------------------------------------------------
        System.out.println("PickupScriptAction execute - entity: " + entity);
        boolean pickedUp = map.getHotbarUI().addItem(itemImage);
        System.out.println("pickedUp: " + pickedUp);

        if (pickedUp && entity != null) {
            entity.setIsHidden(true);
            entity.setIsUncollidable(true);
            System.out.println("isHidden now: " + entity.isHidden());
        }

        return ScriptState.COMPLETED;
    }
    
}