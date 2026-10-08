package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// This class is for the credits screen
public class CreditsScreen extends Screen {
    protected ScreenCoordinator screenCoordinator;
    protected Map background;
    protected KeyLocker keyLocker = new KeyLocker();
    protected SpriteFont creditsLabel;
    protected SpriteFont createdByLabel;
    protected SpriteFont griffinCredit;
    protected SpriteFont hernestCredit;
    protected SpriteFont laurenCredit;
    protected SpriteFont robertoCredit;
    protected SpriteFont returnInstructionsLabel;

    public CreditsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // setup graphics on screen (background map, spritefont text)
        background = new TitleScreenMap();
        background.setAdjustCamera(false);
        try {
            Font secretSolver = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("src/Resources/secret_solver.ttf"))).deriveFont(50f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(secretSolver);
            creditsLabel = new SpriteFont("Credits", 350, 25, secretSolver, Color.white);
            createdByLabel = new SpriteFont("Developed by", 320, 75, secretSolver, Color.white);
            griffinCredit = new SpriteFont("Griffin", 360, 125, secretSolver, Color.white);
            hernestCredit = new SpriteFont("Hernest", 360, 175, secretSolver, Color.white);
            laurenCredit = new SpriteFont("Lauren", 360, 225, secretSolver, Color.white);
            robertoCredit = new SpriteFont("Roberto", 360, 275, secretSolver, Color.white);
            returnInstructionsLabel = new SpriteFont("Press [ESC] to return to the menu", 20, 532, secretSolver, Color.white);
        } catch (IOException | FontFormatException e) {
            System.out.println("ERROR: CREDITS FONT FILE NOT FOUND");
        }
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
        creditsLabel.draw(graphicsHandler);
        createdByLabel.draw(graphicsHandler);
        griffinCredit.draw(graphicsHandler);
        hernestCredit.draw(graphicsHandler);
        laurenCredit.draw(graphicsHandler);
        robertoCredit.draw(graphicsHandler);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
