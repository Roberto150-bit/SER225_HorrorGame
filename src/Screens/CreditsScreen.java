package Screens;

import Engine.*;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.Map;
import Maps.TitleScreenMap;
import SpriteFont.SpriteFont;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    // CC-BY-4.0 requires crediting the 3D models used in the jumpscares
    protected SpriteFont specimenCredit;
    protected SpriteFont specimenLicense;
    protected SpriteFont tallFigureCredit;
    protected SpriteFont tallFigureLicense;
    protected SpriteFont slasherCredit;
    protected SpriteFont slasherLicense;
    protected SpriteFont wendigoCredit;
    protected SpriteFont wendigoLicense;
    protected SpriteFont returnInstructionsLabel;
    protected BufferedImage menuImage;
    public CreditsScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    @Override
    public void initialize() {
        // setup graphics on screen (background map, spritefont text)
        menuImage = ImageLoader.loadWithAlpha("TitleScreenImage.png");
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
            Font smallFont = new Font("Arial", Font.PLAIN, 14);
            specimenCredit = new SpriteFont("\"Specimen 11 Rig\" (sketchfab.com/3d-models/specimen-11-rig-d4543214b37e4c6e9ef5526844c377de)", 20, 340, smallFont, Color.lightGray);
            specimenLicense = new SpriteFont("by NO DONT EAT ME CASEOH (Ferris wheel), licensed under CC-BY-4.0", 20, 358, smallFont, Color.lightGray);
            tallFigureCredit = new SpriteFont("\"tall figure in dark\" (sketchfab.com/3d-models/tall-figure-in-dark-93cee5d144e14e5babbdafd5dcf0a411)", 20, 384, smallFont, Color.lightGray);
            tallFigureLicense = new SpriteFont("by nano nova, licensed under CC-BY-4.0", 20, 402, smallFont, Color.lightGray);
            slasherCredit = new SpriteFont("\"Slasher (The LAST thing you'd SEE.)\" by The WarVet, licensed under CC-BY-4.0", 20, 428, smallFont, Color.lightGray);
            slasherLicense = new SpriteFont("(sketchfab.com/3d-models/slasher-the-last-thing-youd-see-31cb3c3865234ecf9f8e1bf28ca227ab)", 20, 446, smallFont, Color.lightGray);
            wendigoCredit = new SpriteFont("\"Wendigo - inspired by Until Dawn\" by marc3_C, licensed under CC-BY-4.0", 20, 472, smallFont, Color.lightGray);
            wendigoLicense = new SpriteFont("(sketchfab.com/3d-models/wendigo-inspired-by-until-dawn-d46093a707a24444af1ed7d10aa7198f)", 20, 490, smallFont, Color.lightGray);
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
        graphicsHandler.drawImage(menuImage, 0, 0, 800, 1000);
        creditsLabel.draw(graphicsHandler);
        createdByLabel.draw(graphicsHandler);
        griffinCredit.draw(graphicsHandler);
        hernestCredit.draw(graphicsHandler);
        laurenCredit.draw(graphicsHandler);
        robertoCredit.draw(graphicsHandler);
        specimenCredit.draw(graphicsHandler);
        specimenLicense.draw(graphicsHandler);
        tallFigureCredit.draw(graphicsHandler);
        tallFigureLicense.draw(graphicsHandler);
        slasherCredit.draw(graphicsHandler);
        slasherLicense.draw(graphicsHandler);
        wendigoCredit.draw(graphicsHandler);
        wendigoLicense.draw(graphicsHandler);
        returnInstructionsLabel.draw(graphicsHandler);
    }
}
