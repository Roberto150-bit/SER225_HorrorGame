package Engine;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import Puzzles.PuzzleConfig;
import Puzzles.PuzzleResult;
import Puzzles.PuzzleController;
import Puzzles.PuzzleFactory;
import Puzzles.PuzzleManager;
import Puzzles.PuzzleId;
import Puzzles.PuzzleAssets;

public class PuzzleUI {
    
    private boolean isOpen = false;
    private boolean solved = false;

    // Stores the configuration of the currently opened puzzle.
    private PuzzleConfig currentPuzzle;
    private PuzzleController controller; //controls the active minigame
    private PuzzleResult pendingResult;
    private boolean alreadyCompleted = false;

    // Spirit Mirror completion animation.
    private long mirrorSolvedAt = -1L;

    private static final long MIRROR_REVEAL_MS = 2000;
    private static final long MIRROR_FADE_MS = 600;

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

    private boolean completionSoundPlayed = false;

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
        completionSoundPlayed = false;
        mirrorSolvedAt = -1L;
        backgroundImage = null;
        controller = null;

        // Load custom artwork for puzzle-specific interfaces.
        if (config.getPuzzleType() == PuzzleId.SYMBOL_LOCK) {
            backgroundImage = PuzzleAssets.get(
                "SymbolLock/symbol_lock_panel.png"
            );
        }
        
        if (config.getPuzzleType() == PuzzleId.TUNE_RADIO) {
            backgroundImage = PuzzleAssets.get(
                "TuneRadio/tune_radio_panel.png"
            );
        }

        if (config.getPuzzleType() == PuzzleId.HAUNTED_CLOCK) {
            backgroundImage = PuzzleAssets.get(
                "HauntedClock/haunted_clock_panel.png"
            );
        }

        if (config.getPuzzleType() == PuzzleId.RITUAL_SEQUENCE) {
            backgroundImage = PuzzleAssets.get(
                "RitualSequence/ritual_sequence_panel.png"
            );
        }

        if (config.getPuzzleType() == PuzzleId.SPIRIT_MIRROR) {
            backgroundImage = PuzzleAssets.get(
                "SpiritMirror/spirit_mirror_panel.png"
            );
        }

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

                if (!completionSoundPlayed) {
                    SoundPlayer.play("PuzzleComplete.wav");
                    completionSoundPlayed = true;
                }

                if (mirrorSolvedAt == -1L) {
                    mirrorSolvedAt = System.currentTimeMillis();
                }
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

        // Draw the existing puzzle background.
        if (backgroundImage != null) {
            graphicsHandler.drawImage(
                backgroundImage, x, y, width, height
            );
        } else {
            graphicsHandler.drawFilledRectangle(
                x, y, width, height,
                new Color(35, 30, 35)
            );
        }

        // Shared window border.
        graphicsHandler.drawRectangle(
            x, y, width, height,
            new Color(165, 135, 90), 2
        );

        // Only show a generic title for puzzles without
        // their own illustrated header.
        if (backgroundImage == null) {
            graphicsHandler.drawString(
                "PUZZLE",
                x + 25,
                y + 45,
                new Font("Serif", Font.BOLD, 25),
                new Color(220, 190, 135)
            );
        }

        // Keep the existing fallback interface.
        if (controller == null && !alreadyCompleted) {

            graphicsHandler.drawString(
                "Restore power",
                x + 50,
                y + 150,
                new Font("Serif", Font.PLAIN, 18),
                Color.WHITE
            );

            graphicsHandler.drawFilledRectangle(
                activateX, activateY,
                activateWidth, activateHeight,
                new Color(65, 55, 55)
            );

            graphicsHandler.drawString(
                "ACTIVATE",
                activateX + 45,
                activateY + 32,
                new Font("Serif", Font.BOLD, 18),
                Color.WHITE
            );

            graphicsHandler.drawString(
                solved ? "Status: COMPLETE" : "Status: INCOMPLETE",
                x + 160,
                y + 280,
                new Font("Serif", Font.BOLD, 18),
                solved ? new Color(190, 175, 125) : Color.WHITE
            );
        }

        // Render the active minigame.
        if (controller != null && !alreadyCompleted) {
            controller.getMinigame().draw(graphicsHandler);
        }

        // Display a shared completed interface.
        // Works for newly solved and previously solved puzzles.
        boolean showCompletion = solved || alreadyCompleted;
        float completionAlpha = 1.0f;

        if (showCompletion && !alreadyCompleted) {

            if (mirrorSolvedAt == -1L) {
                mirrorSolvedAt = System.currentTimeMillis();
            }

            long elapsed =
                System.currentTimeMillis() - mirrorSolvedAt;

            if (elapsed < MIRROR_REVEAL_MS) {
                showCompletion = false;
            } else {
                completionAlpha = Math.min(
                    1.0f,
                    (float)(elapsed - MIRROR_REVEAL_MS)
                        / MIRROR_FADE_MS
                );
            }
        }

        if (showCompletion) {

            java.awt.Graphics2D g =
                graphicsHandler.getGraphics();

            java.awt.Composite previousComposite =
                g.getComposite();

            g.setComposite(
                java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER,
                    completionAlpha
                )
            );


            int panelX = x + 55;
            int panelY = y + 115;
            int panelWidth = width - 110;
            int panelHeight = 125;

            // Dark Gothic completion panel.
            graphicsHandler.drawFilledRectangle(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                new Color(30, 25, 30)
            );

            // Antique gold border.
            graphicsHandler.drawRectangle(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                new Color(180, 145, 90),
                2
            );

            // Decorative heading.
            graphicsHandler.drawString(
                "✦",
                x + 238,
                panelY + 30,
                new Font("Serif", Font.BOLD, 22),
                new Color(200, 170, 110)
            );

            graphicsHandler.drawString(
                "PUZZLE COMPLETED",
                x + 113,
                panelY + 70,
                new Font("Serif", Font.BOLD, 23),
                new Color(220, 190, 135)
            );

            graphicsHandler.drawString(
                "The seal has been broken.",
                x + 150,
                panelY + 98,
                new Font("Serif", Font.ITALIC, 16),
                new Color(185, 175, 160)
            );

            // Restore transparency for subsequent UI elements.
            g.setComposite(previousComposite);
        }

        // Always draw the close button last.
        graphicsHandler.drawFilledRectangle(
            closeX,
            closeY,
            closeWidth,
            closeHeight,
            new Color(125, 35, 40)
        );

        graphicsHandler.drawRectangle(
            closeX,
            closeY,
            closeWidth,
            closeHeight,
            new Color(200, 165, 110),
            1
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
