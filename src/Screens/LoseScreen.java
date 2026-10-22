package Screens;

import Engine.*;
import Level.Player;
import SpriteFont.SpriteFont;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// This class is for the win level screen
public class LoseScreen extends Screen {
    protected SpriteFont deadMessage;
    protected SpriteFont instructions;
    protected KeyLocker keyLocker = new KeyLocker();
    protected PlayLevelScreen playLevelScreen;

    public LoseScreen(PlayLevelScreen playLevelScreen) {
        this.playLevelScreen = playLevelScreen;
        initialize();
    }

    @Override
    public void initialize() {
        try {
            Font secretSolver = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("src/Resources/secret_solver.ttf"))).deriveFont(40f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(secretSolver);
            deadMessage = new SpriteFont("You Died!", 350, 239, secretSolver, Color.white);
            instructions = new SpriteFont("Space to play again or [ESC] to go back to the main menu", 50, 279, secretSolver, Color.white);
        } catch (IOException | FontFormatException | NullPointerException e) {
            System.out.println("ERROR: TEXT FILE NOT FOUND");
        }
        
        keyLocker.lockKey(Key.SPACE);
        keyLocker.lockKey(Key.ESC);
    }

    @Override
    public void update() {
        if (Keyboard.isKeyUp(Key.SPACE)) {
            keyLocker.unlockKey(Key.SPACE);
        }
        if (Keyboard.isKeyUp(Key.ESC)) {
            keyLocker.unlockKey(Key.ESC);
        }

        // if space is pressed, reset level. if escape is pressed, go back to main menu
        if (Keyboard.isKeyDown(Key.SPACE) && !keyLocker.isKeyLocked(Key.SPACE)) {
            playLevelScreen.resetLevel();
            Player.setHealth(3);
        } else if (Keyboard.isKeyDown(Key.ESC) && !keyLocker.isKeyLocked(Key.ESC)) {
            playLevelScreen.goBackToMenu();
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        graphicsHandler.drawFilledRectangle(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight(), Color.black);
        deadMessage.draw(graphicsHandler);
        instructions.draw(graphicsHandler);
    }
}
