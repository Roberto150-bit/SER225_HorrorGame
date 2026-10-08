package Engine;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleResult;
import Puzzles.PuzzleController;
import Puzzles.PuzzleFactory;
import Puzzles.PuzzleManager;

public class PuzzleUI {
    
    private boolean isOpen = false;
    private boolean solved = false;

    // Stores the configuration of the currently opened puzzle.
    private PuzzleConfig currentPuzzle;
    private PuzzleController controller; //controls the active minigame
    private PuzzleResult pendingResult;
    private boolean alreadyCompleted = false;

    private BufferedImage backgroundImage;

    private int x = 150;
    private int y = 100;
    private int width = 500;
    private int height = 350;

    private int closeX = 610;
    private int closeY = 115;
    private int closeWidth = 25;
    private int closeHeight = 25;

    private int activateX = 300;
    private int activateY = 275;
    private int activateWidth = 180;
    private int activateHeight = 50;

    public void open() {
        solved = false;
        isOpen = true;
        Mouse.showCursor();
    }

    // Opens a specific puzzle using its configuration.
    public void open(PuzzleConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Puzzle config cannot be null");
        }

        // Reset UI state for the selected puzzle instance.
        currentPuzzle = config;
        pendingResult = null;
        backgroundImage = null;
        controller = null;

        // Check whether this specific puzzle was solved before.
        alreadyCompleted = PuzzleManager.isCompleted(
            config.getInstanceId()
        );

        // Only create a minigame if it hasn't been completed.
        if (!alreadyCompleted &&
            PuzzleFactory.isRegistered(config.getPuzzleType())) {

            controller = PuzzleFactory.create(config);
            controller.start();
        }

        open();
    }




    // Returns the configuration of the currently selected puzzle.
    public PuzzleConfig getCurrentPuzzle() {
        return currentPuzzle;
    }

    
    // Called only after the player actually solves a minigame.
    public void completePuzzle() {
        if (isOpen && currentPuzzle != null && pendingResult == null) {
            pendingResult = new PuzzleResult(currentPuzzle, true);
            solved = true;
        }
    }
    
    // Assigns the minigame controller when a puzzle opens.
    public void setController(PuzzleController controller) {
        this.controller = controller;
    }

    // Sets a custom background for the puzzle window.
    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
    }

    // Returns the completed result once, then clears it.
    public PuzzleResult takeResult() {
        PuzzleResult result = pendingResult;
        pendingResult = null;
        return result;
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

        if (controller != null) {
            controller.update();

            PuzzleResult result = controller.takeResult();

            if (result != null) {
                pendingResult = result;
                solved = true;
            }
        }
       
        if (Mouse.isLeftClicked()) {

            int mouseX = Mouse.getMouseX();
            int mouseY = Mouse.getMouseY();

            // Check the close button first.
            boolean clickedCloseButton =
                mouseX >= closeX &&
                mouseX <= closeX + closeWidth &&
                mouseY >= closeY &&
                mouseY <= closeY + closeHeight;

            if (clickedCloseButton) {
                close();
                Mouse.resetClick();
                return;
            }

            // Handle the placeholder when no minigame exists.
            if (controller == null) {

                boolean clickedActivateButton =
                    mouseX >= activateX &&
                    mouseX <= activateX + activateWidth &&
                    mouseY >= activateY &&
                    mouseY <= activateY + activateHeight;

                if (clickedActivateButton) {
                    solved = true;
                }

            } else {

                // Forward clicks inside the puzzle window.
                boolean insidePuzzle =
                    mouseX >= x &&
                    mouseX <= x + width &&
                    mouseY >= y &&
                    mouseY <= y + height;

                if (insidePuzzle) {
                    controller.onClick(mouseX, mouseY);
                }
            }

            // Prevent the click from being processed twice.
            Mouse.resetClick();
        }

    }

    
    public void draw(GraphicsHandler graphicsHandler) {

        if (!isOpen) {
            return;
        }

        // Draw the custom background or use the default.
        if (backgroundImage != null) {
            graphicsHandler.drawImage(
                backgroundImage, x, y, width, height
            );
        } else {
            graphicsHandler.drawFilledRectangle(
                x, y, width, height, new Color(40, 40, 40)
            );
        }

        // Shared border for every puzzle.
        graphicsHandler.drawRectangle(
            x, y, width, height, Color.WHITE, 3
        );

        // Shared title.
        graphicsHandler.drawString(
            "Puzzle",
            x + 25,
            y + 45,
            new Font("Arial", Font.BOLD, 24),
            Color.WHITE
        );

        // Display the placeholder only when no minigame exists.
        if (controller == null && !alreadyCompleted) {

            graphicsHandler.drawString(
                "Restore power",
                x + 50,
                y + 150,
                new Font("Arial", Font.PLAIN, 18),
                Color.WHITE
            );

            graphicsHandler.drawFilledRectangle(
                activateX, activateY,
                activateWidth, activateHeight,
                Color.GRAY
            );

            graphicsHandler.drawString(
                "ACTIVATE",
                activateX + 45,
                activateY + 32,
                new Font("Arial", Font.BOLD, 18),
                Color.WHITE
            );

            graphicsHandler.drawString(
                solved ? "Status: COMPLETE" : "Status: INCOMPLETE",
                x + 160,
                y + 280,
                new Font("Arial", Font.BOLD, 18),
                solved ? Color.GREEN : Color.WHITE
            );
        }

        // Draw the actual minigame when available.
        if (controller != null) {
            controller.getMinigame().draw(graphicsHandler);
        }

        // Show the completed state if this puzzle was solved before.
        if (alreadyCompleted) {
            graphicsHandler.drawString(
                "PUZZLE COMPLETED",
                x + 100,
                y + 170,
                new Font("Arial", Font.BOLD, 24),
                Color.GREEN
            );
        }

        // Draw the close button last so it stays visible.
        graphicsHandler.drawFilledRectangle(
            closeX, closeY,
            closeWidth, closeHeight,
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
