package Engine;

import Puzzles.SealCode;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

// the journal's pages
// it keeps every page the player has found, and the arrows (or LEFT/RIGHT keys) flip between them
// one UI is shared by every room, so the journal remembers what was picked up anywhere in the dungeon
public class UI {
    private Font secretSolverFont;

    // page 0 is the controls page the player starts with, pages 1-5 are the story notes
    public static final int CONTROLS_PAGE = 0;
    public static final int STORY_PAGE_COUNT = 5;
    // the page listing the final seal's code digits, shown once a code note has been found
    public static final int CODE_PAGE = 100;

    private final boolean[] storyPagesFound = new boolean[STORY_PAGE_COUNT + 1];
    private int currentPage = CONTROLS_PAGE;

    // page turning arrows, drawn at the bottom of the left page
    private static final int PREV_ARROW_X = 118;
    private static final int NEXT_ARROW_X = 318;
    private static final int ARROW_Y = 440;
    private static final int ARROW_SIZE = 34;
    private static final Color ARROW_COLOR = new Color(40, 22, 18);
    private static final Color ARROW_DISABLED_COLOR = new Color(40, 22, 18, 60);
    private static final Font PAGE_NUMBER_FONT = new Font("Serif", Font.BOLD, 18);
    private static final Font CODE_DIGIT_FONT = new Font("Serif", Font.BOLD, 40);

    private final KeyLocker keyLocker = new KeyLocker();
    private boolean showingCursor = false;

    public UI() {
        try {
            // Loads custom text for the book
            Font secretSolver = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("src/Resources/secret_solver.ttf"))).deriveFont(Font.PLAIN, 30f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(secretSolver);
            secretSolverFont = secretSolver;
        } catch (IOException | FontFormatException e) {
            System.out.println("ERROR: AN EXCEPTION HAS OCCURED FOR UI.JAVA");
        }
        keyLocker.lockKey(Key.LEFT);
        keyLocker.lockKey(Key.RIGHT);
    }

    // a story note was picked up: keep its page and turn to it
    public void addStoryPage(int page) {
        if (page >= 1 && page <= STORY_PAGE_COUNT) {
            storyPagesFound[page] = true;
            currentPage = page;
        }
    }

    // a code note was picked up: turn to the code page (it appears once any digit has been found)
    public void showCodePage() {
        currentPage = CODE_PAGE;
    }

    // every page the player can flip through, in order
    private ArrayList<Integer> getPages() {
        ArrayList<Integer> pages = new ArrayList<>();
        pages.add(CONTROLS_PAGE);
        for (int page = 1; page <= STORY_PAGE_COUNT; page++) {
            if (storyPagesFound[page]) {
                pages.add(page);
            }
        }
        if (SealCode.hasFoundAnyDigit()) {
            pages.add(CODE_PAGE);
        }
        return pages;
    }

    // page turning while the journal is open
    // puzzleOpen is passed so the mouse cursor isn't hidden while a puzzle needs it
    public void update(boolean bookOpen, boolean puzzleOpen) {
        // the cursor shows while the journal is open so the arrows can be clicked
        if (bookOpen && !showingCursor) {
            Mouse.showCursor();
            showingCursor = true;
        }
        else if (!bookOpen && showingCursor) {
            if (!puzzleOpen) {
                Mouse.hideCursor();
            }
            showingCursor = false;
        }

        if (!bookOpen) {
            return;
        }

        if (pressed(Key.LEFT)) {
            turnPage(-1);
        }
        if (pressed(Key.RIGHT)) {
            turnPage(1);
        }

        if (Mouse.isLeftClicked()) {
            int mouseX = Mouse.getMouseX();
            int mouseY = Mouse.getMouseY();
            if (isOnArrow(mouseX, mouseY, PREV_ARROW_X)) {
                turnPage(-1);
                Mouse.resetClick(); // so the hotbar doesn't also use the click
            }
            else if (isOnArrow(mouseX, mouseY, NEXT_ARROW_X)) {
                turnPage(1);
                Mouse.resetClick();
            }
        }
    }

    private void turnPage(int direction) {
        ArrayList<Integer> pages = getPages();
        int index = Math.max(0, pages.indexOf(currentPage));
        int newIndex = Math.max(0, Math.min(pages.size() - 1, index + direction));
        if (newIndex != index) {
            currentPage = pages.get(newIndex);
            SoundPlayer.play("bookOpening.wav");
        }
    }

    // true only on the frame the key goes down
    private boolean pressed(Key key) {
        if (Keyboard.isKeyUp(key)) {
            keyLocker.unlockKey(key);
            return false;
        }
        if (!keyLocker.isKeyLocked(key)) {
            keyLocker.lockKey(key);
            return true;
        }
        return false;
    }

    private boolean isOnArrow(int x, int y, int arrowX) {
        return x >= arrowX && x <= arrowX + ARROW_SIZE && y >= ARROW_Y && y <= ARROW_Y + ARROW_SIZE;
    }

    public void draw(GraphicsHandler graphicsHandler, boolean bookOpen) {
        if (!bookOpen) {
            return;
        }

        drawPage(graphicsHandler, currentPage);

        ArrayList<Integer> pages = getPages();
        int index = Math.max(0, pages.indexOf(currentPage));
        drawArrow(graphicsHandler, PREV_ARROW_X, true, index > 0);
        drawArrow(graphicsHandler, NEXT_ARROW_X, false, index < pages.size() - 1);

        String pageNumber = (index + 1) + " / " + pages.size();
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(PAGE_NUMBER_FONT).stringWidth(pageNumber);
        int middle = (PREV_ARROW_X + NEXT_ARROW_X + ARROW_SIZE) / 2;
        graphicsHandler.drawString(pageNumber, middle - textWidth / 2, ARROW_Y + 23, PAGE_NUMBER_FONT, ARROW_COLOR);
    }

    // a filled triangle pointing left or right, faded when there's no page that way
    private void drawArrow(GraphicsHandler graphicsHandler, int x, boolean pointsLeft, boolean enabled) {
        int[] xs = pointsLeft
                ? new int[] {x + ARROW_SIZE - 6, x + ARROW_SIZE - 6, x + 6}
                : new int[] {x + 6, x + 6, x + ARROW_SIZE - 6};
        int[] ys = {ARROW_Y + 5, ARROW_Y + ARROW_SIZE - 5, ARROW_Y + ARROW_SIZE / 2};
        graphicsHandler.getGraphics().setColor(enabled ? ARROW_COLOR : ARROW_DISABLED_COLOR);
        graphicsHandler.getGraphics().fillPolygon(xs, ys, 3);
    }

    private void drawPage(GraphicsHandler graphicsHandler, int page) {
        if (page == CODE_PAGE) {
            graphicsHandler.drawString("Scraps of a code...", 115,120, secretSolverFont, Color.black);
            // the journal font has no numbers, so the digits use a plain font
            graphicsHandler.drawString(SealCode.getFoundDigits(), 140,200, CODE_DIGIT_FONT, Color.black);
            graphicsHandler.drawString("The seal in the far\nroom wants all five\n" +
                                    "\n" +
                                    "Remember them", 115,260, secretSolverFont, Color.black);
        }
        else if (page == CONTROLS_PAGE) {
            graphicsHandler.drawString("Esc to pause game \nWASD to Move \nSPACE to Continue\nE to Interact \nC to Open/Close Journal\nArrows to Turn Pages", 115,100, secretSolverFont, Color.black);
        }
        else if (page == 1) {
            graphicsHandler.drawString("I am sorry for the souls \n who rest here. \n \n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "It’s my fault\t  It’s my fault\r\n" + //
                                    "       ", 115,120, secretSolverFont, Color.black);
        } else if (page == 2) {
            graphicsHandler.drawString("Don’t be tricked by the \nhuman face\r\n" + //
                                    "\r\n" + //
                                    "Don’t look too closely\r\n" + //
                                    "\r\n" + //
                                    "Don’t investigate\r\n" + //
                                    "\r\n" + //
                                    "Keep your head down\r\n" + //
                                    "\r\n" + //
                                    "Don’t ask for help\r\n" + //
                                    " ", 115,120, secretSolverFont, Color.black);
        } else if (page == 3) {
            graphicsHandler.drawString("“It” created something \nnew. Why?\r\n" + //
                                    "\r\n" + //
                                    "What kind of crazy project…\r\n" + //
                                    "\r\n" + //
                                    "It’s too late now\r\n" + //
                                    "\r\n" + //
                                    "If I was more decisive..\r\n" + //
                                    "", 110,120, secretSolverFont, Color.black);
        } else if (page == 4) {
            graphicsHandler.drawString("I saw it morph, it stood \nthere\r\n" + //
                                    "\r\n" + //
                                    "In the middle of the room\r\n" + //
                                    "\r\n" + //
                                    "Strong winds around it\r\n" + //
                                    "\r\n" + //
                                    "Just what.. Is that thing…\r\n" + //
                                    " ", 115,120, secretSolverFont, Color.black);
        } else if (page == 5) {
            graphicsHandler.drawString("My eyes deceive me\r\n" + //
                                    "\r\n" + //
                                    "Why can I see… I didn’t \nwant to see\r\n" + //
                                    "\r\n" + //
                                    "Please bring me back\r\n" + //
                                    "\r\n" + //
                                    "I want to go back\r\n" + //
                                    "\r\n" + //
                                    "I’m so scared\r\n" + //
                                    " ", 115,120, secretSolverFont, Color.black);
        }
    }
}
