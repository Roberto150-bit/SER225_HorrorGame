package Screens;

import Engine.*;

import java.awt.*;

// This class is for the lose (death) screen
// shows a menu where the player can restart from their last checkpoint, start Chapter 1 over, go back to the main menu, or quit
public class LoseScreen extends Screen {
    private static final int RESTART_FROM_CHECKPOINT = 0;
    private static final int RESTART_CHAPTER = 1;
    private static final int MAIN_MENU = 2;
    private static final int QUIT = 3;

    protected PlayLevelScreen playLevelScreen;
    protected OverlayMenu menu = new OverlayMenu("You Died!", new Color(200, 30, 30),
            "Restart from Checkpoint", "Restart Chapter", "Main Menu", "Quit Game");
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
            case RESTART_FROM_CHECKPOINT:
                Mouse.hideCursor();
                playLevelScreen.restartFromCheckpoint();
                break;
            case RESTART_CHAPTER:
                Mouse.hideCursor();
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
