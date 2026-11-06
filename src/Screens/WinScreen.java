package Screens;

import Engine.*;

import java.awt.*;

// The "Chapter 1 Complete" card, shown over the Necromancer's room after it falls and the closing dialogue plays.
// The title and closing lines fade in, then the player can continue (and look around, the game carries on from that room)
// or go back to the main menu.
public class WinScreen extends Screen {
    private static final int CONTINUE = 0;
    private static final int MAIN_MENU = 1;

    private static final String TITLE = "CHAPTER 1 COMPLETE";
    private static final String[] CLOSING_LINES = {
        "Dean survived the night beneath the house.",
        "But the Tenant is still awake, and Mara is still waiting.",
        "Dean's story continues..."
    };

    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 44);
    private static final Font LINE_FONT = new Font("Serif", Font.ITALIC, 20);
    private static final Color TITLE_COLOR = new Color(220, 190, 135);
    private static final Color LINE_COLOR = new Color(205, 195, 180);

    // frames before each part fades in, and how long each fade takes
    private static final int FADE_FRAMES = 60;
    private static final int LINE_DELAY = 70;
    // the title fades in first, then each line LINE_DELAY frames after the one before, then the menu
    private static final int MENU_DELAY = FADE_FRAMES + LINE_DELAY * (CLOSING_LINES.length - 1) + FADE_FRAMES;

    protected PlayLevelScreen playLevelScreen;
    private final OverlayMenu menu = new OverlayMenu("", TITLE_COLOR, "Continue", "Main Menu");
    // how dark the room gets behind the card
    private static final int BACKDROP_ALPHA = 200;
    private int timer = 0;
    private boolean menuOpen = false;

    public WinScreen(PlayLevelScreen playLevelScreen) {
        this.playLevelScreen = playLevelScreen;
        initialize();
    }

    @Override
    public void initialize() {
        menu.setHint("Click an option, or use UP/DOWN and ENTER");
        // below the closing lines
        menu.setOffsetY(110);
    }

    // called when the chapter ends so the screen starts from the beginning
    public void open() {
        timer = 0;
        menuOpen = false;
    }

    @Override
    public void update() {
        timer++;
        if (timer < MENU_DELAY) {
            return;
        }
        if (!menuOpen) {
            // opened after the text, so keys held from the last line of dialogue don't pick an option
            menu.open();
            menuOpen = true;
        }

        switch (menu.update()) {
            case CONTINUE:
                playLevelScreen.continueAfterChapter();
                break;
            case MAIN_MENU:
                playLevelScreen.goBackToMenu();
                break;
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        int width = ScreenManager.getScreenWidth();
        // the room stays faintly visible behind the card, fading darker as the card comes up
        int backdrop = Math.min(BACKDROP_ALPHA, timer * BACKDROP_ALPHA / FADE_FRAMES);
        graphicsHandler.drawFilledRectangle(0, 0, width, ScreenManager.getScreenHeight(), new Color(0, 0, 0, backdrop));
        // the menu dims everything under it, so it goes first and the text is drawn over it
        if (menuOpen) {
            menu.draw(graphicsHandler);
        }

        drawCentered(graphicsHandler, TITLE, 110, TITLE_FONT, fade(TITLE_COLOR, timer));
        for (int i = 0; i < CLOSING_LINES.length; i++) {
            int lineTimer = timer - FADE_FRAMES - LINE_DELAY * i;
            drawCentered(graphicsHandler, CLOSING_LINES[i], 170 + i * 32, LINE_FONT, fade(LINE_COLOR, lineTimer));
        }
    }

    private static Color fade(Color color, int framesShown) {
        int alpha = Math.max(0, Math.min(255, framesShown * 255 / FADE_FRAMES));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private void drawCentered(GraphicsHandler graphicsHandler, String text, int y, Font font, Color color) {
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(font).stringWidth(text);
        graphicsHandler.drawString(text, (ScreenManager.getScreenWidth() - textWidth) / 2, y, font, color);
    }
}
