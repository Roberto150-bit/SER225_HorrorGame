package GameObject;

//In order to display the book as a GIF I need to cycle through the frames.

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;



public class Book {
    private BufferedImage image;
    private boolean isActive = true;
    private Key interactKey = Key.C; // key to interact with the book
    private KeyLocker keyLocker = new KeyLocker();

    //Frame stuff
    private List<BufferedImage> frames = new ArrayList<>();
    private int currentFrame = 0;
    private int frameTimer = 0;
    private int framesPerImage = 12; // lower = faster animation, higher = slower
    private static final int FRAME_COUNT = 7;
    //For reusing same photos, saving resources.
    private List<Integer> playOrder = new ArrayList<>(); 
    private int playOrderIndex = 0;



    public Book() {
        loadFrames("src/Resources/BookFramesPNG/Intensity", FRAME_COUNT);        buildPingPongOrder();
    }

    // Goes through images 1,2,3,4,5,6,7,6,5,4,3,2,1 for animation
    private void buildPingPongOrder() {
        playOrder.clear();
        for (int i = 0; i < frames.size(); i++) {
            playOrder.add(i);
        }
        for (int i = frames.size() - 2; i > 0; i--) {
            playOrder.add(i);
        }
    }



    private void loadFrames(String basePath, int frameCount) {

        for (int i = 1; i <= frameCount; i++) {
            File file = new File(basePath + i + ".png");
            try {
                BufferedImage frame = ImageIO.read(file);
                if (frame == null) {
                    System.err.println("Unreadable image: " + file.getPath());
                    continue;
                }
                frames.add(frame);
            } catch (IOException e) {
                System.err.println("Could not load frame: " + file.getPath());
                e.printStackTrace();
            }
        }
    }


    public void update() {
        if (Keyboard.isKeyDown(interactKey) && !keyLocker.isKeyLocked(interactKey)) {
            keyLocker.lockKey(interactKey);
            isActive = !isActive; // toggle open/closed
        }
        else if (Keyboard.isKeyUp(interactKey)) {
            keyLocker.unlockKey(interactKey);
        }

        if (isActive && !playOrder.isEmpty()) {
            frameTimer++;
            if (frameTimer >= framesPerImage) {
                frameTimer = 0;
                playOrderIndex = (playOrderIndex + 1) % playOrder.size();
            }
        }

    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public BufferedImage getImage() {//Used in the draw method to know which frame to display.
        if (frames.isEmpty() || playOrder.isEmpty()) {
            return null;
        }
        return frames.get(playOrder.get(playOrderIndex));
    }

    public void draw(GraphicsHandler graphicsHandler){
        if (isActive && !frames.isEmpty() && !playOrder.isEmpty()) {
            BufferedImage current = frames.get(playOrder.get(playOrderIndex));
            graphicsHandler.drawImage(current, -155, 50, 1100, 480); // Draws one frame at a time.
        }

    }


}
