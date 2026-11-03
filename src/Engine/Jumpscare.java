package Engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

// full screen jumpscare, played when an enemy kills the player
// a flickering glimpse of the creature in the dark, then a slam cut to its face with a scream, shake and a flash
// creatures with grab images grab the player in between: a short rendered sequence of it reaching out until its hands fill the view
// PlayLevelScreen freezes the game while it plays, then goes straight to the lose screen
public class Jumpscare {
    // which creature jumps out
    public enum Type {
        // WeirdEnemy. "Specimen 11 Rig" by NO DONT EAT ME CASEOH (Ferris wheel), CC-BY-4.0, see Resources/specimen_11_rig/license.txt
        SPECIMEN_11("SpecimenScareApproach.png", "SpecimenScareFace.png", "jumpscareScream.wav", new Color(200, 0, 0)),
        // CreepyStickEnemy. "tall figure in dark" by nano nova, CC-BY-4.0, see Resources/tall_figure_in_dark/license.txt
        TALL_FIGURE("TallFigureScareApproach.png", "TallFigureScareFace.png", "tallFigureScream.wav", new Color(230, 235, 255)),
        // NecromancerBoss. "Slasher (The LAST thing you'd SEE.)" by The WarVet, CC-BY-4.0
        SLASHER("SlasherScareApproach.png", "SlasherScareFace.png", "slasherScream.wav", new Color(150, 0, 0)),
        // Ghost and CosmicCreeper share the wendigo, each with its own scream and flash; it reaches out and grabs the player
        // "Wendigo - inspired by Until Dawn" by marc3_C, CC-BY-4.0
        GHOST("WendigoScareApproach.png", "WendigoScareFace.png", "ghostScream.wav", new Color(230, 235, 255), "WendigoGrab", 7),
        CREEPER("WendigoScareApproach.png", "WendigoScareFace.png", "creeperScream.wav", new Color(170, 0, 0), "WendigoGrab", 7);

        private final String approachImageFile;
        private final String faceImageFile;
        private final String soundFile;
        private final Color flashColor;
        // the grab: images <grabImagePrefix>0.png, 1.png, ... played in order between the glimpse and the face
        private final String grabImagePrefix; // null for creatures that don't grab
        private final int grabImageCount;
        private BufferedImage approachImage;
        private BufferedImage faceImage;
        private BufferedImage[] grabImages;

        Type(String approachImageFile, String faceImageFile, String soundFile, Color flashColor) {
            this(approachImageFile, faceImageFile, soundFile, flashColor, null, 0);
        }

        Type(String approachImageFile, String faceImageFile, String soundFile, Color flashColor, String grabImagePrefix, int grabImageCount) {
            this.approachImageFile = approachImageFile;
            this.faceImageFile = faceImageFile;
            this.soundFile = soundFile;
            this.flashColor = flashColor;
            this.grabImagePrefix = grabImagePrefix;
            this.grabImageCount = grabImageCount;
        }

        private void loadImages() {
            if (approachImage == null) {
                approachImage = ImageLoader.loadWithAlpha(approachImageFile);
                faceImage = ImageLoader.loadWithAlpha(faceImageFile);
                grabImages = new BufferedImage[grabImageCount];
                for (int i = 0; i < grabImageCount; i++) {
                    grabImages[i] = ImageLoader.loadWithAlpha(grabImagePrefix + i + ".png");
                }
            }
        }

        // the last grab image (its hands filling the view) is held a little longer
        private int grabFrames() {
            return grabImageCount == 0 ? 0 : grabImageCount * FRAMES_PER_GRAB_IMAGE + GRAB_HOLD_FRAMES;
        }
    }

    // timeline in frames (60 per second)
    private static final int APPROACH_FRAMES = 16;
    private static final int FRAMES_PER_GRAB_IMAGE = 3; // only for creatures that grab
    private static final int GRAB_HOLD_FRAMES = 5;
    private static final int FACE_FRAMES = 50;
    private static final int SHAKE = 14;
    private static final int FLASH_FRAMES = 8;

    private static final Random random = new Random();
    private static Type current;
    private static int timer = -1;

    // starts the jumpscare, restarting it if one is already on screen
    public static void play(Type type) {
        type.loadImages();
        current = type;
        timer = 0;
    }

    public static boolean isPlaying() {
        return timer >= 0;
    }

    // ends the jumpscare right away
    public static void stop() {
        timer = -1;
    }

    public static void update() {
        if (!isPlaying()) {
            return;
        }
        // the scream starts as it grabs (or as the face appears, for creatures that don't grab)
        if (timer == APPROACH_FRAMES) {
            SoundPlayer.play(current.soundFile);
        }
        timer++;
        if (timer >= APPROACH_FRAMES + current.grabFrames() + FACE_FRAMES) {
            timer = -1;
        }
    }

    public static void draw(GraphicsHandler graphicsHandler) {
        if (!isPlaying()) {
            return;
        }
        int width = Config.GAME_WINDOW_WIDTH;
        int height = Config.GAME_WINDOW_HEIGHT;

        if (timer < APPROACH_FRAMES) {
            // a flickering glimpse of it creeping closer in the dark
            graphicsHandler.drawFilledRectangle(0, 0, width, height, Color.BLACK);
            boolean flickerOff = timer % 5 == 3;
            if (!flickerOff) {
                float progress = timer / (float) APPROACH_FRAMES;
                drawScaled(graphicsHandler, current.approachImage, 0.8f + 0.35f * progress, 0, 0);
            }
        } else if (timer < APPROACH_FRAMES + current.grabFrames()) {
            // it lunges and grabs: the rendered grab frames play in order, shaking harder as its hands reach the view
            int grabTimer = timer - APPROACH_FRAMES;
            int image = Math.min(current.grabImageCount - 1, grabTimer / FRAMES_PER_GRAB_IMAGE);
            float progress = (grabTimer + 1) / (float) current.grabFrames();
            int shakeX = Math.round((random.nextFloat() * 2 - 1) * SHAKE * progress);
            int shakeY = Math.round((random.nextFloat() * 2 - 1) * SHAKE * progress);
            graphicsHandler.drawFilledRectangle(0, 0, width, height, Color.BLACK);
            drawScaled(graphicsHandler, current.grabImages[image], 1.05f, shakeX, shakeY);
        } else {
            int faceTimer = timer - APPROACH_FRAMES - current.grabFrames();
            graphicsHandler.drawFilledRectangle(0, 0, width, height, Color.BLACK);
            // slams in oversized then settles, shaking less as it goes
            float slam = Math.max(0f, 1f - faceTimer / 10f);
            float shake = SHAKE * Math.max(0.15f, 1f - faceTimer / (float) FACE_FRAMES);
            int shakeX = Math.round((random.nextFloat() * 2 - 1) * shake);
            int shakeY = Math.round((random.nextFloat() * 2 - 1) * shake);
            drawScaled(graphicsHandler, current.faceImage, 1.05f + 0.3f * slam, shakeX, shakeY);
            if (faceTimer < FLASH_FRAMES) {
                int flashAlpha = Math.round(170 * (1f - faceTimer / (float) FLASH_FRAMES));
                Color flash = current.flashColor;
                graphicsHandler.drawFilledRectangle(0, 0, width, height, new Color(flash.getRed(), flash.getGreen(), flash.getBlue(), flashAlpha));
            }
        }
    }

    // draws the image centered on the screen at the given zoom
    private static void drawScaled(GraphicsHandler graphicsHandler, BufferedImage image, float zoom, int offsetX, int offsetY) {
        int drawWidth = Math.round(Config.GAME_WINDOW_WIDTH * zoom);
        int drawHeight = Math.round(Config.GAME_WINDOW_HEIGHT * zoom);
        int x = (Config.GAME_WINDOW_WIDTH - drawWidth) / 2 + offsetX;
        int y = (Config.GAME_WINDOW_HEIGHT - drawHeight) / 2 + offsetY;
        graphicsHandler.drawImage(image, x, y, drawWidth, drawHeight);
    }

    // prevents Jumpscare from being instantiated
    private Jumpscare() { }
}
