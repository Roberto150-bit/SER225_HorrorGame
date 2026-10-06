package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import GameObject.Sprite;
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
        settingsLabel = new SpriteFont("Settings", 350, 25, "Chalkduster", 30, Color.white);
        options = new SpriteFont("Movement", 130, 120, "Chalkduster", 30, Color.white);
        walkForward = new SpriteFont("Keybind: W", 130, 170, "Chalkduster", 30, Color.white);
        walkLeft = new SpriteFont("Keybind: A", 130, 220, "Chalkduster", 30, Color.white);
        walkRight = new SpriteFont("Keybind: D", 130, 270, "Chalkduster", 30, Color.white);
        walkBehind = new SpriteFont("Keybind: D", 130, 320, "Chalkduster", 30, Color.white);
        returnInstructionsLabel = new SpriteFont("Press [ESC] to return to the menu", 20, 532, "Chalkduster", 30, Color.white);
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
        walkForward.draw(graphicsHandler);
        walkLeft.draw(graphicsHandler);
        walkRight.draw(graphicsHandler);
        walkBehind.draw(graphicsHandler);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
