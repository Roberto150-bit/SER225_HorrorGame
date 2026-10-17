
package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public class SettingsScreen extends Screen {

    private final ScreenCoordinator screenCoordinator;
    private BufferedImage menuImage;

    private int selected = 0;
    private boolean waitingForKey = false;
    private boolean navigationHeld = false;
    private boolean enterHeld = true;

    private String message = "Select a control using UP/DOWN";

    private static final String[] ACTIONS = {
        "Move Up", "Move Left", "Move Down", "Move Right"
    };

    public SettingsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        menuImage = ImageLoader.loadWithAlpha("dark_forest.png");

        selected = 0;
        waitingForKey = false;
        navigationHeld = false;
        enterHeld = true;
        message = "Select a control using UP/DOWN";

        Keyboard.clearLastPressedKey();
    }

    @Override
    public void update() {

        // Capture a new key when rebinding.
        if (waitingForKey) {
            Key key = Keyboard.pollLastPressedKey();

            if (key == null) {
                return;
            }

            if (key == Key.ESC) {
                waitingForKey = false;
                message = "Rebinding canceled";
            }
            else if (KeyBindings.set(selected, key)) {
                waitingForKey = false;
                message = "Updated " + ACTIONS[selected]
                    + " to " + key;
            }
            else {
                message = "Key unavailable or already in use";
            }

            return;
        }

        Keyboard.clearLastPressedKey();

        // Return to the main menu.
        if (Keyboard.isKeyDown(Key.ESC)) {
            screenCoordinator.setGameState(GameState.MENU);
            return;
        }

        // Move through the available controls.
        boolean up = Keyboard.isKeyDown(Key.UP);
        boolean down = Keyboard.isKeyDown(Key.DOWN);

        if (!up && !down) {
            navigationHeld = false;
        }

        if (!navigationHeld && (up || down)) {
            selected = (selected + (down ? 1 : 3)) % 4;
            navigationHeld = true;
        }

        // Start changing the selected control.
        boolean enter = Keyboard.isKeyDown(Key.ENTER);

        if (!enter) {
            enterHeld = false;
        }

        if (enter && !enterHeld) {
            waitingForKey = true;
            enterHeld = true;
            message = "Press a new letter key (ESC to cancel)";
            Keyboard.clearLastPressedKey();
        }
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        // Existing menu background.
        if (menuImage != null) {
            graphicsHandler.drawImage(
                menuImage, 0, 0, 800, 605
            );
        } else {
            graphicsHandler.drawFilledRectangle(
                0, 0, 800, 605, new Color(25, 20, 30)
            );
        }

        graphicsHandler.drawString(
            "SETTINGS - KEYBINDS",
            150, 105,
            new Font("Serif", Font.BOLD, 32),
            Color.WHITE
        );

        // Display movement controls.
        for (int i = 0; i < ACTIONS.length; i++) {

            int y = 170 + i * 65;

            if (i == selected) {
                graphicsHandler.drawFilledRectangle(
                    145, y - 27, 460, 43,
                    new Color(90, 28, 40)
                );
            }

            graphicsHandler.drawString(
                ACTIONS[i] + ": " + KeyBindings.get(i),
                170, y,
                new Font("Serif", Font.BOLD, 23),
                i == selected
                    ? new Color(255, 210, 160)
                    : Color.WHITE
            );
        }

        graphicsHandler.drawString(
            message,
            105, 460,
            new Font("Arial", Font.BOLD, 17),
            Color.WHITE
        );

        graphicsHandler.drawString(
            "UP/DOWN: select   ENTER: change   ESC: back",
            80, 530,
            new Font("Arial", Font.BOLD, 15),
            Color.LIGHT_GRAY
        );
    }
}
