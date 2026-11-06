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
// the equipped weapon (lit up red) is used with SPACE or a left click, and stays equipped after each use
// press 1-6, or click a slot, to equip something else; picking up the first weapon equips it automatically
// single use items (relics) flash green and disappear, then the player goes back to their knife or bow
// E is left to the player for interacting (doors, notes, pickups)
public class HotbarUI {

    private BufferedImage slotBackgroundImage;
    private BufferedImage heartImage;

    private Key attackKey = Key.SPACE; // key to use the equipped item
    private static final int SLOT_COUNT = 6;
    private static final int HEART_COUNT = Player.MAX_HEALTH;
    private Key[] slotKeys = {Key.ONE, Key.TWO, Key.THREE, Key.FOUR, Key.FIVE, Key.SIX}; // key to select each slot

    private KeyLocker keyLocker = new KeyLocker();

    private BufferedImage[] itemImages = new BufferedImage[SLOT_COUNT];

    private BufferedImage[][] itemFrames = new BufferedImage[SLOT_COUNT][]; // animation frames, null for static items
    private int[] frameDelays = new int[SLOT_COUNT]; // milliseconds per frame
    private ItemUse[] itemUses = new ItemUse[SLOT_COUNT];

    private static final int HEART_SIZE = 60;
    private static final int HEART_SPACING = 65;
    private static final int SLOT_SIZE = 75;
    private static final int SLOT_SPACING = 65; // slots overlap a little since the ring image has empty space around it
    private static final int ITEM_SIZE = 60;
    private static final int ITEM_OFFSET_Y = 10; // items sit a little lower than the top of the slot ring

    private int[] xSlots = new int[SLOT_COUNT]; // X positions for each slot, set in the constructor
    private int[] xHearts = new int[HEART_COUNT]; // X positions for each heart

    private int yHeart = 420;
    private int ySlot = 490; // Y position for all slots

    private static final Color EQUIPPED_COLOR = new Color(255, 0, 0, 110);
    private static final Color USING_COLOR = new Color(0, 255, 0, 140);
    private static final Font SLOT_NUMBER_FONT = new Font("Chalkduster", Font.BOLD, 13);
    private static final Font HINT_FONT = new Font("Arial", Font.BOLD, 14);
    private static final String CONTROLS_HINT = "SPACE / CLICK  attack     1-6  switch weapon     E  interact";

    private static final int USE_FLASH_FRAMES = 20; // how long a slot stays green after its item is used (also the time between attacks)
    private static final int HINT_FRAMES = 60 * 12; // the controls hint shows for 12 seconds after the first weapon is picked up

    private int equippedSlot = -1; // -1 when nothing usable is equipped
    private int hintTimer = 0;
    private boolean hintShown = false;
    private int usingSlot = -1; // slot currently flashing green, -1 when none
    private int useTimer = 0;
    private ItemUse usedItem; // set the frame an item is used so the map can apply its effect
    private BufferedImage usedItemImage; // image of the last used item



    public HotbarUI() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            xSlots[i] = i * SLOT_SPACING;
        }

        try {
            slotBackgroundImage = ImageIO.read(getClass().getResourceAsStream("/Resources/SingleHotbarSlot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < HEART_COUNT; i++) {
            xHearts[i] = i * HEART_SPACING;
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

    // canTakeInput is false while something else needs the keyboard/mouse (puzzles, textboxes, the journal)
    public void update(boolean canTakeInput) {
        if (hintTimer > 0) {
            hintTimer--;
        }

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
            // a SPACE press or click that closes a textbox or puzzle shouldn't also attack once it's gone
            if (Keyboard.isKeyDown(attackKey)) {
                keyLocker.lockKey(attackKey);
            }
            return;
        }

        for (int i = 0; i < slotKeys.length; i++) {
            if (Keyboard.isKeyDown(slotKeys[i]) && !keyLocker.isKeyLocked(slotKeys[i])) {
                keyLocker.lockKey(slotKeys[i]);
                equip(i);
            }
            else if (Keyboard.isKeyUp(slotKeys[i])) {
                keyLocker.unlockKey(slotKeys[i]);
            }
        }

        if (Keyboard.isKeyDown(attackKey) && !keyLocker.isKeyLocked(attackKey)) {
            keyLocker.lockKey(attackKey);
            attack();
        }
        else if (Keyboard.isKeyUp(attackKey)) {
            keyLocker.unlockKey(attackKey);
        }

        if (Mouse.isLeftClicked()) {
            // clicking a slot equips it, clicking anywhere else attacks
            int clickedSlot = getSlotAt(Mouse.getMouseX(), Mouse.getMouseY());
            if (clickedSlot != -1) {
                equip(clickedSlot);
            } else {
                attack();
            }
            Mouse.resetClick();
        }
    }

    // an independent copy of the inventory (items, which one is equipped), used by checkpoints
    public HotbarUI copy() {
        HotbarUI copy = new HotbarUI();
        copy.itemImages = itemImages.clone();
        copy.itemFrames = itemFrames.clone();
        copy.frameDelays = frameDelays.clone();
        copy.itemUses = itemUses.clone();
        copy.equippedSlot = equippedSlot;
        copy.hintShown = hintShown;
        // a relic that was mid-throw when the copy was made is already gone
        if (usingSlot != -1 && itemUses[usingSlot] == ItemUse.SINGLE_USE) {
            copy.removeItem(usingSlot);
        }
        return copy;
    }

    // true if the bow (or any other reusable ranged weapon) is in the bar
    public boolean hasItem(ItemUse itemUse) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (itemImages[i] != null && itemUses[i] == itemUse) {
                return true;
            }
        }
        return false;
    }

    // only usable items (weapons, relics) can be equipped, notes and such just sit in the bar
    private void equip(int slot) {
        if (itemImages[slot] != null && itemUses[slot] != null && itemUses[slot] != ItemUse.NONE) {
            equippedSlot = slot;
        }
    }

    // uses the equipped item, which stays equipped (relics are removed after their flash, see update)
    private void attack() {
        // nothing equipped, or the last attack is still flashing
        if (equippedSlot == -1 || usingSlot != -1) {
            return;
        }
        usingSlot = equippedSlot;
        useTimer = USE_FLASH_FRAMES;
        usedItem = itemUses[equippedSlot];
        usedItemImage = itemImages[equippedSlot];
    }

    // after a relic is used up, go back to a weapon that stays (knife or bow), if there is one
    private void equipReusableWeapon() {
        equippedSlot = -1;
        for (int i = 0; i < itemImages.length; i++) {
            if (itemImages[i] != null && (itemUses[i] == ItemUse.REUSABLE || itemUses[i] == ItemUse.RANGED)) {
                equippedSlot = i;
                return;
            }
        }
    }

    // a new usable item is equipped automatically if the player isn't holding anything
    private void onItemAdded(int slot) {
        if (itemUses[slot] == ItemUse.NONE) {
            return;
        }
        if (equippedSlot == -1) {
            equippedSlot = slot;
        }
        if (!hintShown) {
            hintShown = true;
            hintTimer = HINT_FRAMES;
        }
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
                onItemAdded(i);
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
                onItemAdded(i);
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
            if (equippedSlot == slotIndex) {
                equipReusableWeapon();
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

            // light up the inside of the ring: red for the equipped item, green while it's being used
            Color highlight = i == usingSlot ? USING_COLOR : i == equippedSlot ? EQUIPPED_COLOR : null;
            if (highlight != null) {
                g.setColor(highlight);
                g.fillOval(xSlots[i] + 10, ySlot + 8, SLOT_SIZE - 20, SLOT_SIZE - 20);
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

            graphicsHandler.drawStringWithOutline(String.valueOf(i + 1), xSlots[i] + 11, ySlot + 16, SLOT_NUMBER_FONT, Color.WHITE, Color.BLACK, 2);
        }

        // how to fight, shown for a while after the first weapon is picked up
        if (hintTimer > 0) {
            graphicsHandler.drawStringWithOutline(CONTROLS_HINT, 8, ySlot - 8, HINT_FONT, Color.WHITE, Color.BLACK, 2);
        }
    }


}
