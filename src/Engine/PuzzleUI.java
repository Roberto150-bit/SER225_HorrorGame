package Engine;

import java.awt.Color;
import java.awt.Font;

public class PuzzleUI {
    
    private boolean isOpen = false;

    private int x = 150;
    private int y = 100;
    private int width = 500;
    private int height = 350;

    private int closeX = 610;
    private int closeY = 115;
    private int closeWidth = 25;
    private int closeHeight = 25;

    public void open() {
        isOpen = true;
        Mouse.showCursor();
    }

    public void close() {
        isOpen = false;
        Mouse.hideCursor();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void update() {

        if (!isOpen) {
            return;
        }

        if (Mouse.isLeftClicked()) {

            int mouseX = Mouse.getMouseX();
            int mouseY = Mouse.getMouseY();

            boolean clickedCloseButton = 
                mouseX >= closeX && 
                mouseX <= closeX + closeWidth &&
                mouseY >= closeY &&
                mouseY <= closeY + closeHeight;
            
            if (clickedCloseButton) {
                close();
            }

            Mouse.resetClick();
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {

        if (!isOpen) {
            return;
        }

        // dark puzzle window
        graphicsHandler.drawFilledRectangle(
            x, 
            y, 
            width, 
            height, 
            new Color(40, 40, 40)
        );

        // border
        graphicsHandler.drawRectangle(
            x, 
            y, 
            width, 
            height, 
            Color.WHITE,
            3
        );

        // title
        graphicsHandler.drawString(
            "Puzzle", 
            x + 25, 
            y + 45, 
            new Font("Arial", Font.BOLD, 24), 
            Color.WHITE
        );

        // Puzzle text
        graphicsHandler.drawString(
            "Puzzle content", 
            x + 50, 
            y + 150, 
            new Font("Arial", Font.PLAIN, 18),
            Color.WHITE
        );

        // Close button
        graphicsHandler.drawFilledRectangle(
            closeX,
            closeY,
            closeWidth,
            closeHeight,
            Color.RED
        );

        graphicsHandler.drawString(
            "X",
            closeX + 7,
            closeY + 19,
            new Font("Arial", Font.BOLD, 16),
            Color.WHITE
        );
    }
}
