package Engine;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Mirror cutscene: Dean looks into the mirror, the reflection flashes into a demon eye, then it cuts back to the normal reflection.
// Works like Jumpscare: PlayLevelScreen freezes the game and calls update() while isPlaying() is true, and calls draw() on top of the world.
// To trigger it from anywhere (a script, the mirror object, etc.) just call MirrorCutscene.start().
public class MirrorCutscene {

    // image paths are relative to Config.RESOURCES_PATH, the same as every other ImageLoader call
    private static final String NORMAL_IMAGE = "mirror_normal.png";
    private static final String DEMON_IMAGE = "mirror_demon.png";

    // press this to skip the cutscene
    private static final Key SKIP_KEY = Key.SPACE;

    // timeline in frames (60 frames per second)
    private static final int DISSOLVE_IN = 30;   // cutscene fades in over the game world
    private static final int NORMAL_HOLD = 150;  // shot 1: normal reflection
    private static final int FLASH = 18;         // white flash, the image swaps at its peak
    private static final int DEMON_HOLD = 120;   // shot 2: demon eye reflection
    private static final int NORMAL_AGAIN = 120; // shot 3:  back to the normal reflection
    private static final int DISSOLVE_OUT = 30;  // cutscene fades out

    // frame numbers where each part starts
    private static final int FLASH_START = DISSOLVE_IN + NORMAL_HOLD;
    private static final int DEMON_START = FLASH_START + FLASH / 2;
    private static final int DEMON_END = FLASH_START + FLASH + DEMON_HOLD;
    private static final int TOTAL = DEMON_END + NORMAL_AGAIN + DISSOLVE_OUT;

    private static BufferedImage normalImage;
    private static BufferedImage demonImage;

    private static boolean playing = false;
    private static int frame = 0;
    // the skip key has to be let go once before it counts, so the key press that started the cutscene can't skip it
    private static boolean skipArmed = false;

    public static void start() {
        if (playing) {
            return;
        }
        if (normalImage == null) {
            normalImage = ImageLoader.loadWithAlpha(NORMAL_IMAGE);
            demonImage = ImageLoader.loadWithAlpha(DEMON_IMAGE);
        }
        playing = true;
        frame = 0;
        skipArmed = false;
    }

    // plays the cutscene the first time it's asked for each game (the mirror puzzle is rebuilt every time it's opened)
    private static boolean seenThisGame = false;

    public static void startOnce() {
        if (!seenThisGame) {
            seenThisGame = true;
            start();
        }
    }

    // called when a new game starts so the cutscene plays again the first time the mirror is used
    public static void resetSeen() {
        seenThisGame = false;
    }

    public static void stop() {
        playing = false;
        frame = 0;
    }

    public static boolean isPlaying() {
        return playing;
    }

    public static void update() {
        if (!playing) {
            return;
        }

        if (Keyboard.isKeyUp(SKIP_KEY)) {
            skipArmed = true;
        } else if (skipArmed) {
            // jump to the final fade-out instead of cutting off abruptly
            frame = Math.max(frame, TOTAL - DISSOLVE_OUT);
            skipArmed = false;
        }

        frame++;
        if (frame >= TOTAL) {
            stop();
        }
    }

    public static void draw(GraphicsHandler graphicsHandler) {
        if (!playing) {
            return;
        }

        Graphics2D g = graphicsHandler.getGraphics();
        Rectangle screen = g.getClipBounds();

        // the whole cutscene layer dissolves in at the start and out at the end
        float layerAlpha = 1f;
        if (frame < DISSOLVE_IN) {
            layerAlpha = frame / (float) DISSOLVE_IN;
        } else if (frame >= TOTAL - DISSOLVE_OUT) {
            layerAlpha = (TOTAL - frame) / (float) DISSOLVE_OUT;
        }
        layerAlpha = Math.max(0f, Math.min(1f, layerAlpha));

        BufferedImage image = (frame >= DEMON_START && frame < DEMON_END) ? demonImage : normalImage;

        // scale to fit the screen without stretching, with black bars if the shapes don't match
        double scale = Math.min(screen.width / (double) image.getWidth(), screen.height / (double) image.getHeight());
        int drawW = (int) (image.getWidth() * scale);
        int drawH = (int) (image.getHeight() * scale);
        int drawX = screen.x + (screen.width - drawW) / 2;
        int drawY = screen.y + (screen.height - drawH) / 2;

        Composite oldComposite = g.getComposite();
        Object oldInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);

        g.setComposite(AlphaComposite.SrcOver.derive(layerAlpha));
        g.setColor(Color.BLACK);
        g.fillRect(screen.x, screen.y, screen.width, screen.height);
        // nearest neighbor keeps the pixel art crisp when it is scaled up
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphicsHandler.drawImage(image, drawX, drawY, drawW, drawH);

        g.setComposite(oldComposite);
        if (oldInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        }

        // white flash between shot 1 and shot 2: ramps up to full at DEMON_START, then back down
        if (frame >= FLASH_START && frame < FLASH_START + FLASH) {
            float p = (frame - FLASH_START) / (float) FLASH;
            int flashAlpha = (int) (255 * (1f - Math.abs(p - 0.5f) * 2f));
            flashAlpha = Math.max(0, Math.min(255, flashAlpha));
            graphicsHandler.drawFilledRectangle(screen.x, screen.y, screen.width, screen.height, new Color(255, 255, 255, flashAlpha));
        }
    }
}
