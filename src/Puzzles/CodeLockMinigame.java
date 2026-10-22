package Puzzles;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

// the final seal: a keypad where the player enters the code pieced together from the notes found around the dungeon
public class CodeLockMinigame implements PuzzleMinigame {

    private static final int CENTER_X = 400;

    // keypad layout: 3 columns x 4 rows, bottom row is CLEAR, 0, ENTER
    private static final String[] KEYS = {
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "CLR", "0", "OK"
    };
    private static final int KEY_WIDTH = 62;
    private static final int KEY_HEIGHT = 40;
    private static final int KEY_GAP = 8;
    private static final int KEYPAD_TOP = 222;

    private static final Color GOLD = new Color(215, 177, 108);
    private static final Color DARK = new Color(38, 26, 35);
    private static final Color INNER = new Color(76, 48, 54);
    private static final Color LIGHT = new Color(245, 221, 175);
    private static final Color WRONG = new Color(200, 50, 50);

    private static final int WRONG_FLASH_FRAMES = 45;

    private final StringBuilder entered = new StringBuilder();
    private boolean solved;
    private int wrongTimer;

    public CodeLockMinigame(PuzzleConfig config) {
        reset();
    }

    @Override
    public void update() {
        if (wrongTimer > 0) {
            wrongTimer--;
        }
    }

    private int getKeyX(int index) {
        int keypadWidth = 3 * KEY_WIDTH + 2 * KEY_GAP;
        return CENTER_X - keypadWidth / 2 + (index % 3) * (KEY_WIDTH + KEY_GAP);
    }

    private int getKeyY(int index) {
        return KEYPAD_TOP + (index / 3) * (KEY_HEIGHT + KEY_GAP);
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        if (solved) {
            return;
        }

        drawCentered(graphicsHandler, "THE FINAL SEAL", 150, new Font("Serif", Font.BOLD, 24), GOLD);
        drawCentered(graphicsHandler, "Enter the code from the notes", 172, new Font("Serif", Font.ITALIC, 15), LIGHT);

        // one slot per digit of the code
        int slotWidth = 34;
        int slotGap = 8;
        int slotsLeft = CENTER_X - (SealCode.LENGTH * slotWidth + (SealCode.LENGTH - 1) * slotGap) / 2;
        Color slotBorder = wrongTimer > 0 ? WRONG : GOLD;
        for (int i = 0; i < SealCode.LENGTH; i++) {
            int slotX = slotsLeft + i * (slotWidth + slotGap);
            graphicsHandler.drawFilledRectangleWithBorder(slotX, 180, slotWidth, 34, DARK, slotBorder, 2);
            if (i < entered.length()) {
                String digit = String.valueOf(entered.charAt(i));
                int digitWidth = graphicsHandler.getGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 24)).stringWidth(digit);
                graphicsHandler.drawString(digit, slotX + (slotWidth - digitWidth) / 2, 205, new Font("Serif", Font.BOLD, 24), LIGHT);
            }
        }

        for (int i = 0; i < KEYS.length; i++) {
            graphicsHandler.drawFilledRectangleWithBorder(getKeyX(i), getKeyY(i), KEY_WIDTH, KEY_HEIGHT, INNER, GOLD, 2);
            Font keyFont = new Font("Serif", Font.BOLD, KEYS[i].length() > 1 ? 15 : 20);
            int keyTextWidth = graphicsHandler.getGraphics().getFontMetrics(keyFont).stringWidth(KEYS[i]);
            graphicsHandler.drawString(KEYS[i], getKeyX(i) + (KEY_WIDTH - keyTextWidth) / 2, getKeyY(i) + 27, keyFont, LIGHT);
        }

        if (wrongTimer > 0) {
            drawCentered(graphicsHandler, "The seal rejects you.", 430, new Font("Serif", Font.ITALIC, 16), WRONG);
        }
    }

    private void drawCentered(GraphicsHandler graphicsHandler, String text, int y, Font font, Color color) {
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(font).stringWidth(text);
        graphicsHandler.drawString(text, CENTER_X - textWidth / 2, y, font, color);
    }

    @Override
    public void onClick(int mouseX, int mouseY) {
        if (solved) {
            return;
        }

        for (int i = 0; i < KEYS.length; i++) {
            if (mouseX >= getKeyX(i) && mouseX <= getKeyX(i) + KEY_WIDTH
                    && mouseY >= getKeyY(i) && mouseY <= getKeyY(i) + KEY_HEIGHT) {
                pressKey(KEYS[i]);
                return;
            }
        }
    }

    public void pressKey(String key) {
        if (key.equals("CLR")) {
            entered.setLength(0);
        }
        else if (key.equals("OK")) {
            checkCode();
        }
        else if (entered.length() < SealCode.LENGTH) {
            entered.append(key);
        }
    }

    private void checkCode() {
        if (entered.toString().equals(SealCode.getCode())) {
            solved = true;
        }
        else {
            wrongTimer = WRONG_FLASH_FRAMES;
            entered.setLength(0);
        }
    }

    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        solved = false;
        wrongTimer = 0;
        entered.setLength(0);
    }
}
