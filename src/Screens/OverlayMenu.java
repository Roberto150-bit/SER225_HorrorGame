package Screens;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.Mouse;
import Engine.ScreenManager;
import java.awt.Color;
import java.awt.Font;

// a panel of clickable buttons drawn over the game, used by the pause menu and the death menu
// buttons can be clicked with the mouse, or picked with UP/DOWN (or W/S) and ENTER/SPACE
// LEFT/RIGHT (or A/D) is passed back through getAdjustDirection() for things like volume sliders
public class OverlayMenu {
    private static final int BUTTON_WIDTH = 340;
    private static final int BUTTON_HEIGHT = 44;
    private static final int BUTTON_GAP = 14;
    private static final int TITLE_SPACE = 80;

    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 40);
    private static final Font BUTTON_FONT = new Font("Serif", Font.BOLD, 22);
    private static final Font HINT_FONT = new Font("Arial", Font.PLAIN, 14);

    private static final Key[] MENU_KEYS = {
        Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT, Key.W, Key.S, Key.A, Key.D, Key.ENTER, Key.SPACE
    };

    private final String title;
    private final String[] options;
    private final Color titleColor;
    private String hint = "";
    private int selected = 0;
    private int adjustDirection = 0;
    private int lastMouseX = -1, lastMouseY = -1;
    private final KeyLocker keyLocker = new KeyLocker();

    public OverlayMenu(String title, Color titleColor, String... options) {
        this.title = title;
        this.titleColor = titleColor;
        this.options = options;
    }

    // call whenever the menu is shown so keys and clicks from gameplay don't pick an option right away
    public void open() {
        selected = 0;
        adjustDirection = 0;
        for (Key key : MENU_KEYS) {
            keyLocker.lockKey(key);
        }
        Mouse.resetClick();
        lastMouseX = Mouse.getMouseX();
        lastMouseY = Mouse.getMouseY();
        Mouse.showCursor();
    }

    public void setOption(int index, String label) {
        options[index] = label;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getSelected() {
        return selected;
    }

    // -1 if LEFT was pressed this frame, 1 if RIGHT was, 0 otherwise
    public int getAdjustDirection() {
        return adjustDirection;
    }

    // returns the index of the option chosen this frame, or -1 if nothing was chosen
    public int update() {
        adjustDirection = 0;

        if (pressed(Key.UP) | pressed(Key.W)) {
            selected = (selected + options.length - 1) % options.length;
        }
        if (pressed(Key.DOWN) | pressed(Key.S)) {
            selected = (selected + 1) % options.length;
        }
        if (pressed(Key.LEFT) | pressed(Key.A)) {
            adjustDirection = -1;
        }
        if (pressed(Key.RIGHT) | pressed(Key.D)) {
            adjustDirection = 1;
        }

        // only let the mouse change the selection when it actually moves, so it doesn't fight the keyboard
        int mouseX = Mouse.getMouseX();
        int mouseY = Mouse.getMouseY();
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            int hovered = getButtonAt(mouseX, mouseY);
            if (hovered != -1) {
                selected = hovered;
            }
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        if (Mouse.isLeftClicked()) {
            Mouse.resetClick();
            int clicked = getButtonAt(Mouse.getMouseX(), Mouse.getMouseY());
            if (clicked != -1) {
                selected = clicked;
                return clicked;
            }
        }

        if (pressed(Key.ENTER) | pressed(Key.SPACE)) {
            return selected;
        }
        return -1;
    }

    // true only on the frame the key goes down
    private boolean pressed(Key key) {
        if (Keyboard.isKeyUp(key)) {
            keyLocker.unlockKey(key);
            return false;
        }
        if (!keyLocker.isKeyLocked(key)) {
            keyLocker.lockKey(key);
            return true;
        }
        return false;
    }

    private int getButtonsTop() {
        int panelHeight = TITLE_SPACE + options.length * (BUTTON_HEIGHT + BUTTON_GAP);
        return (ScreenManager.getScreenHeight() - panelHeight) / 2 + TITLE_SPACE;
    }

    private int getButtonX() {
        return (ScreenManager.getScreenWidth() - BUTTON_WIDTH) / 2;
    }

    private int getButtonY(int index) {
        return getButtonsTop() + index * (BUTTON_HEIGHT + BUTTON_GAP);
    }

    private int getButtonAt(int x, int y) {
        for (int i = 0; i < options.length; i++) {
            if (x >= getButtonX() && x <= getButtonX() + BUTTON_WIDTH
                    && y >= getButtonY(i) && y <= getButtonY(i) + BUTTON_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    public void draw(GraphicsHandler graphicsHandler) {
        int screenWidth = ScreenManager.getScreenWidth();
        int screenHeight = ScreenManager.getScreenHeight();

        // dim the game behind the menu
        graphicsHandler.drawFilledRectangle(0, 0, screenWidth, screenHeight, new Color(0, 0, 0, 170));

        drawCentered(graphicsHandler, title, getButtonsTop() - 30, TITLE_FONT, titleColor);

        for (int i = 0; i < options.length; i++) {
            boolean isSelected = i == selected;
            graphicsHandler.drawFilledRectangleWithBorder(
                getButtonX(), getButtonY(i), BUTTON_WIDTH, BUTTON_HEIGHT,
                isSelected ? new Color(90, 28, 40) : new Color(25, 20, 30, 230),
                isSelected ? new Color(255, 210, 160) : new Color(120, 100, 110),
                2
            );
            drawCentered(graphicsHandler, options[i], getButtonY(i) + 30, BUTTON_FONT,
                isSelected ? new Color(255, 210, 160) : Color.WHITE);
        }

        if (!hint.isEmpty()) {
            drawCentered(graphicsHandler, hint, getButtonY(options.length) + 20, HINT_FONT, Color.LIGHT_GRAY);
        }
    }

    private void drawCentered(GraphicsHandler graphicsHandler, String text, int y, Font font, Color color) {
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(font).stringWidth(text);
        graphicsHandler.drawString(text, (ScreenManager.getScreenWidth() - textWidth) / 2, y, font, color);
    }
}
