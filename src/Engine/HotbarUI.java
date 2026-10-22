package Engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import Level.Player;

// the player's 6 slot inventory bar
// press 1-6 to select a slot (it lights up red), then use the item in it (it flashes green) by:
//   pressing E, double tapping the same number, or left clicking (clicking right on a slot uses that slot)
// pressing the selected number again slowly deselects it
// single use items disappear after the green flash, reusable items stay
public class HotbarUI {

    private BufferedImage slotBackgroundImage;
    private BufferedImage heartImage;

    private Key useKey = Key.E; // key to use the selected item
    private static final int SLOT_COUNT = 6;
    private static final int HEART_COUNT = Player.MAX_HEALTH;
    private Key[] slotKeys = {Key.ONE, Key.TWO, Key.THREE, Key.FOUR, Key.FIVE, Key.SIX}; // key to select each slot

    private KeyLocker keyLocker = new KeyLocker();

    private BufferedImage[] itemImages = new BufferedImage[SLOT_COUNT];

    private BufferedImage[][] itemFrames = new BufferedImage[SLOT_COUNT][]; // animation frames, null for static items
    private int[] frameDelays = new int[SLOT_COUNT]; // milliseconds per frame
    private ItemUse[] itemUses = new ItemUse[SLOT_COUNT];

    private static final int HEART_SIZE = 36;
    private static final int HEART_SPACING = 40;
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_SPACING = 42; // slots overlap a little since the ring image has empty space around it
    private static final int ITEM_SIZE = 38;
    private static final int ITEM_OFFSET_Y = 6; // items sit a little lower than the top of the slot ring
    private static final int HOTBAR_MARGIN = 8; // gap from the left edge of the screen

    private int[] xSlots = new int[SLOT_COUNT]; // X positions for each slot, set in the constructor
    private int[] xHearts = new int[HEART_COUNT]; // X positions for each heart

    private static final int HEART_MARGIN = 20;
    private int yHeart = 14;
    private int ySlot = 517; // Y position for all slots (bottom of the bar stays where the bigger one ended)

    private static final Color SELECTED_COLOR = new Color(255, 0, 0, 110);
    private static final Color USING_COLOR = new Color(0, 255, 0, 140);
    private static final Font SLOT_NUMBER_FONT = new Font("Arial", Font.BOLD, 11);

    private static final int DOUBLE_TAP_FRAMES = 25; // max frames between taps of a number for it to count as a double tap
    private static final int USE_FLASH_FRAMES = 20; // how long a slot stays green after its item is used

    private int frameCount = 0;
    private int selectedSlot = -1; // -1 when no slot is selected
    private int lastSlotTapFrame = 0;
    private int usingSlot = -1; // slot currently flashing green, -1 when none
    private int useTimer = 0;
    private ItemUse usedItem; // set the frame an item is used so the map can apply its effect
    private BufferedImage usedItemImage; // image of the last used item



    public HotbarUI() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            xSlots[i] = HOTBAR_MARGIN + i * SLOT_SPACING;
        }

        try {
            slotBackgroundImage = ImageIO.read(getClass().getResourceAsStream("/Resources/SingleHotbarSlot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // hearts sit in the top right corner, the XP bar is in the top left
        int heartsLeft = Config.GAME_WINDOW_WIDTH - HEART_MARGIN - (HEART_COUNT - 1) * HEART_SPACING - HEART_SIZE;
        for(int i = 0; i < HEART_COUNT; i++) {
            xHearts[i] = heartsLeft + i * HEART_SPACING;
        }

        try {
            heartImage = ImageIO.read(getClass().getResourceAsStream("/Resources/heartIcon.png"));
            if (heartImage == null) {
                System.out.println("The heart image could not be found for HOTBARUI.java");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load heart icon from " + Config.RESOURCES_PATH, e);
        }
    }

    // canTakeInput is false while something else needs the keyboard/mouse (puzzles, textboxes)
    public void update(boolean canTakeInput) {
        frameCount++;

        // finish the green flash, then remove single use items
        if (usingSlot != -1) {
            useTimer--;
            if (useTimer <= 0) {
                if (itemUses[usingSlot] == ItemUse.SINGLE_USE) {
                    removeItem(usingSlot);
                }
                usingSlot = -1;
            }
        }

        if (!canTakeInput) {
            return;
        }

        for (int i = 0; i < slotKeys.length; i++) {
            if (Keyboard.isKeyDown(slotKeys[i]) && !keyLocker.isKeyLocked(slotKeys[i])) {
                keyLocker.lockKey(slotKeys[i]);
                onSlotKeyTapped(i);
            }
            else if (Keyboard.isKeyUp(slotKeys[i])) {
                keyLocker.unlockKey(slotKeys[i]);
            }
        }

        if (Keyboard.isKeyDown(useKey) && !keyLocker.isKeyLocked(useKey)) {
            keyLocker.lockKey(useKey);
            if (selectedSlot != -1) {
                useItem(selectedSlot);
            }
        }
        else if (Keyboard.isKeyUp(useKey)) {
            keyLocker.unlockKey(useKey);
        }

        if (Mouse.isLeftClicked()) {
            int clickedSlot = getSlotAt(Mouse.getMouseX(), Mouse.getMouseY());
            if (clickedSlot != -1 && itemImages[clickedSlot] != null) {
                useItem(clickedSlot);
            } else if (selectedSlot != -1) {
                useItem(selectedSlot);
            }
            Mouse.resetClick();
        }
    }

    private void onSlotKeyTapped(int slot) {
        if (slot == selectedSlot) {
            if (frameCount - lastSlotTapFrame <= DOUBLE_TAP_FRAMES) {
                useItem(slot);
            } else {
                selectedSlot = -1;
            }
        } else {
            // only slots holding an item can be selected, tapping an empty one clears the selection
            selectedSlot = itemImages[slot] != null ? slot : -1;
        }
        lastSlotTapFrame = frameCount;
    }

    private void useItem(int slot) {
        // nothing happens while another item is flashing
        if (usingSlot != -1) {
            return;
        }
        // items that can't be used (like notes) just get deselected, so the interact key goes back to normal
        if (itemImages[slot] == null || itemUses[slot] == ItemUse.NONE) {
            selectedSlot = -1;
            return;
        }
        usingSlot = slot;
        useTimer = USE_FLASH_FRAMES;
        usedItem = itemUses[slot];
        usedItemImage = itemImages[slot];
        selectedSlot = -1;
    }

    // returns the slot under the given screen position, or -1 if there isn't one
    private int getSlotAt(int x, int y) {
        for (int i = xSlots.length - 1; i >= 0; i--) { // slots overlap a little, the rightmost one is drawn on top
            if (x >= xSlots[i] && x < xSlots[i] + SLOT_SIZE && y >= ySlot && y < ySlot + SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    // true while a slot is selected, so the player's interact key is left to the hotbar
    public boolean hasSelectedItem() {
        return selectedSlot != -1;
    }

    // returns the type of item used since the last call (null if none), so the map can apply its effect only once
    public ItemUse takeUsedItem() {
        ItemUse item = usedItem;
        usedItem = null;
        return item;
    }

    public BufferedImage getUsedItemImage() {
        return usedItemImage;
    }



    public boolean addItem(BufferedImage itemImage) {
        return addItem(itemImage, ItemUse.NONE);
    }

    public boolean addItem(BufferedImage itemImage, ItemUse itemUse) {
        for (int i = 0; i < itemImages.length; i++) {
            if (itemImages[i] == null) {
                itemImages[i] = itemImage;
                itemUses[i] = itemUse;
                return true; // Places in first open slot, ends loop by returning true
            }
        }
        return false; // hotbar full
    }

    // adds an item that animates while in the hotbar
    public boolean addAnimatedItem(BufferedImage[] frames, int frameDelay) {
        return addAnimatedItem(frames, frameDelay, ItemUse.NONE);
    }

    public boolean addAnimatedItem(BufferedImage[] frames, int frameDelay, ItemUse itemUse) {
        for (int i = 0; i < itemImages.length; i++) {
            if (itemImages[i] == null) {
                itemImages[i] = frames[0];
                itemFrames[i] = frames;
                frameDelays[i] = frameDelay;
                itemUses[i] = itemUse;
                return true;
            }
        }
        return false;
    }

    public void removeItem(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < itemImages.length) {
            itemImages[slotIndex] = null;
            itemFrames[slotIndex] = null;
            itemUses[slotIndex] = null;
            if (selectedSlot == slotIndex) {
                selectedSlot = -1;
            }
        }
    }


    public void draw(GraphicsHandler graphicsHandler, int health){
        Graphics2D g = graphicsHandler.getGraphics();

        for (int i = 0; i < Math.min(Math.max(health, 0), HEART_COUNT); i++) {
            graphicsHandler.drawImage(heartImage, xHearts[i], yHeart, HEART_SIZE, HEART_SIZE);
        }

        for (int i = 0; i < xSlots.length; i++) {
            if (slotBackgroundImage != null) {
                graphicsHandler.drawImage(slotBackgroundImage, xSlots[i], ySlot, SLOT_SIZE, SLOT_SIZE);
            }

            // light up the inside of the ring: red when selected, green while the item is being used
            Color highlight = i == usingSlot ? USING_COLOR : i == selectedSlot ? SELECTED_COLOR : null;
            if (highlight != null) {
                g.setColor(highlight);
                g.fillOval(xSlots[i] + 6, ySlot + 5, SLOT_SIZE - 13, SLOT_SIZE - 13);
            }

            // draw the item on top, if this slot has one
            if (itemImages[i] != null) {
                BufferedImage image = itemImages[i];
                if (itemFrames[i] != null) {
                    int frame = (int) ((System.currentTimeMillis() / frameDelays[i]) % itemFrames[i].length);
                    image = itemFrames[i][frame];
                }
                graphicsHandler.drawImage(image, xSlots[i] + (SLOT_SIZE - ITEM_SIZE) / 2, ySlot + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE);
            }

            graphicsHandler.drawStringWithOutline(String.valueOf(i + 1), xSlots[i] + 6, ySlot + 12, SLOT_NUMBER_FONT, Color.WHITE, Color.BLACK, 2);
        }

    }


}
