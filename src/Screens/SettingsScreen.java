package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;

public class SettingsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected Map background;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont settingsLabel;
    protected SpriteFont options;
    protected SpriteFont returnInstructionsLabel;

    public SettingsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        background = new TitleScreenMap();
        background.setAdjustCamera(false);
        settingsLabel = new SpriteFont("Settings", 50, 7, "None", 30, Color.white);
        options = new SpriteFont("Developed by Griffin, Hernest, Lauren, and Robert", 130, 121, "Times New Roman", 20, Color.white);
        returnInstructionsLabel = new SpriteFont("Press [ESC] to return to the menu", 20, 532, "Times New Roman", 30, Color.white);
        keyLocker.lockKey(Key.SPACE);

        
        keyLocker.lockKey(Key.SPACE);
        
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
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
