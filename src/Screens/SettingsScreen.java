package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import GameObject.Sprite;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SettingsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected Map background;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont settingsLabel;
    protected SpriteFont options;
    protected SpriteFont walkForward;
    protected SpriteFont walkLeft;
    protected SpriteFont walkRight;
    protected SpriteFont walkBehind;
    protected SpriteFont returnInstructionsLabel;

    public SettingsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        background = new TitleScreenMap();
        background.setAdjustCamera(false);
        try {
            Font secretSolver = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("src/Resources/secret_solver.ttf"))).deriveFont(50f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(secretSolver);
            settingsLabel = new SpriteFont("Settings", 350, 25, secretSolver, Color.white);
            options = new SpriteFont("Movement", 130, 120, secretSolver, Color.white);
            walkForward = new SpriteFont("Keybind: W", 130, 170, secretSolver, Color.white);
            walkLeft = new SpriteFont("Keybind: A", 130, 220, secretSolver, Color.white);
            walkRight = new SpriteFont("Keybind: S", 130, 270, secretSolver, Color.white);
            walkBehind = new SpriteFont("Keybind: D", 130, 320, secretSolver, Color.white);
            returnInstructionsLabel = new SpriteFont("Press [ESC] to return to the menu", 20, 532, secretSolver, Color.white);
            keyLocker.lockKey(Key.SPACE);
        } catch(IOException | FontFormatException e) {
            System.out.println("ERROR: SETTINGS FONT NOT FOUND");
        }
    }

    public void update() {
        background.update(null);

        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }

        // if ESC is pressed, go back to main menu
        if (!keyLocker.isKeyLocked(Key.ESC) && Keyboard.isKeyDown(Key.ESC)) {
            screenCoordinator.setGameState(GameState.MENU);
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        background.draw(graphicsHandler);
        settingsLabel.draw(graphicsHandler);
        options.draw(graphicsHandler);
        walkForward.draw(graphicsHandler);
        walkLeft.draw(graphicsHandler);
        walkRight.draw(graphicsHandler);
        walkBehind.draw(graphicsHandler);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
