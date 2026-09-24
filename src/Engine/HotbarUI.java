package Engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;


public class HotbarUI {

    private BufferedImage slotBackgroundImage;

    private boolean isActive;
    private Key pickUpKey = Key.E; // key to pick up items
    private Key dropKey = Key.Q; // key to drop items

    private KeyLocker keyLocker = new KeyLocker();

    private BufferedImage[] itemImages = new BufferedImage[3]; 

    private int[]xSlots = {0, 65, 130}; // X positions for each slot

    private int ySlot = 490; // Y position for all slots



    public HotbarUI() {

        try {
            slotBackgroundImage = ImageIO.read(getClass().getResourceAsStream("/Resources/SingleHotbarSlot.png"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (Keyboard.isKeyDown(pickUpKey) && !keyLocker.isKeyLocked(pickUpKey)) {
            keyLocker.lockKey(pickUpKey);
            isActive = !isActive; // toggle open/closed
        }
        else if (Keyboard.isKeyUp(pickUpKey)) {
            keyLocker.unlockKey(pickUpKey);
        }

        if (Keyboard.isKeyDown(dropKey) && !keyLocker.isKeyLocked(dropKey)) {
            keyLocker.lockKey(dropKey);
            isActive = !isActive; // toggle open/closed
        }
        else if (Keyboard.isKeyUp(dropKey)) {
            keyLocker.unlockKey(dropKey);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean addItem(BufferedImage itemImage) {
        for (int i = 0; i < itemImages.length; i++) {
            if (itemImages[i] == null) {
                itemImages[i] = itemImage;
                return true; // Places in first open slot, ends loop by returning true
            }
        }
        return false; // hotbar full
    }

    public void removeItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < itemImages.length) {
            itemImages[slotIndex] = null;
        }
    }


    public void draw(GraphicsHandler graphicsHandler){
        if (isActive && slotBackgroundImage != null) {
            graphicsHandler.drawImage(slotBackgroundImage, 0, 490, 75,75); 
            graphicsHandler.drawImage(slotBackgroundImage, 65, 490, 75,75); 
            graphicsHandler.drawImage(slotBackgroundImage, 130, 490, 75,75); 

        }

        for (int i = 0; i < xSlots.length; i++) {
            // draw the empty slot frame
            //if (slotBackgroundImage != null) {
            //    graphicsHandler.drawImage(slotBackgroundImage, xSlots[i], ySlot, 75, 75);
            //}
            // draw the item on top, if this slot has one
            if (itemImages[i] != null) {
                graphicsHandler.drawImage(itemImages[i], xSlots[i], ySlot, 75, 75);
            }
        }

    }


}