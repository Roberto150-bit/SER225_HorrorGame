package Screens;

import Engine.*;
import Level.Player;

import java.awt.*;

// This class is for the lose (death) screen
// shows a menu where the player can click to try again, go back to the main menu, or quit
public class LoseScreen extends Screen {
    private static final int TRY_AGAIN = 0;
    private static final int MAIN_MENU = 1;
    private static final int QUIT = 2;

    protected PlayLevelScreen playLevelScreen;
    protected OverlayMenu menu = new OverlayMenu("You Died!", new Color(200, 30, 30),
            "Try Again", "Main Menu", "Quit Game");
    protected KeyLocker keyLocker = new KeyLocker();

    public LoseScreen(PlayLevelScreen playLevelScreen) {
        this.playLevelScreen = playLevelScreen;
        initialize();
    }

    @Override
    public void initialize() {
        menu.setHint("Click an option, or use UP/DOWN and ENTER");
    }

    // called when the player dies so the menu starts fresh
    public void open() {
        menu.open();
        keyLocker.lockKey(Key.ESC);
    }

    @Override
    public void update() {
        if (Keyboard.isKeyUp(Key.ESC)) {
            keyLocker.unlockKey(Key.ESC);
        }
        // ESC is still a shortcut back to the main menu
        if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
            playLevelScreen.goBackToMenu();
            return;
        }

        switch (menu.update()) {
            case TRY_AGAIN:
                Mouse.hideCursor();
                Player.setHealth(3);
                playLevelScreen.resetLevel();
                break;
            case MAIN_MENU:
                playLevelScreen.goBackToMenu();
                break;
            case QUIT:
                System.exit(0);
                break;
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        graphicsHandler.drawFilledRectangle(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), Color.black);
        menu.draw(graphicsHandler);
    }
}
