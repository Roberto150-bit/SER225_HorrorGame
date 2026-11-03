package Engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

// full screen jumpscare, played when an enemy kills the player
// a flickering glimpse of the creature in the dark, then a slam cut to its face with a scream, shake and a flash
// PlayLevelScreen freezes the game while it plays, then goes straight to the lose screen
public class Jumpscare {
    // which creature jumps out
    public enum Type {
        // WeirdEnemy. "Specimen 11 Rig" by NO DONT EAT ME CASEOH (Ferris wheel), CC-BY-4.0, see Resources/specimen_11_rig/license.txt
        SPECIMEN_11("SpecimenScareApproach.png", "SpecimenScareFace.png", "jumpscareScream.wav", new Color(200, 0, 0)),
        // CreepyStickEnemy. "tall figure in dark" by nano nova, CC-BY-4.0, see Resources/tall_figure_in_dark/license.txt
        TALL_FIGURE("TallFigureScareApproach.png", "TallFigureScareFace.png", "tallFigureScream.wav", new Color(230, 235, 255)),
        // NecromancerBoss. "Slasher (The LAST thing you'd SEE.)" by The WarVet, CC-BY-4.0
        SLASHER("SlasherScareApproach.png", "SlasherScareFace.png", "slasherScream.wav", new Color(150, 0, 0));

        private final String approachImageFile;
        private final String faceImageFile;
        private final String soundFile;
        private final Color flashColor;
        private BufferedImage approachImage;
        private BufferedImage faceImage;

        Type(String approachImageFile, String faceImageFile, String soundFile, Color flashColor) {
            this.approachImageFile = approachImageFile;
            this.faceImageFile = faceImageFile;
            this.soundFile = soundFile;
            this.flashColor = flashColor;
        }

        private void loadImages() {
            if (approachImage == null) {
                approachImage = ImageLoader.loadWithAlpha(approachImageFile);
                faceImage = ImageLoader.loadWithAlpha(faceImageFile);
            }
        }
    }

    // timeline in frames (60 per second)
    private static final int APPROACH_FRAMES = 16;
    private static final int FACE_FRAMES = 50;
    private static final int TOTAL_FRAMES = APPROACH_FRAMES + FACE_FRAMES;
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
        if (timer == APPROACH_FRAMES) {
            SoundPlayer.play(current.soundFile);
        }
        timer++;
        if (timer >= TOTAL_FRAMES) {
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
        } else {
            int faceTimer = timer - APPROACH_FRAMES;
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
