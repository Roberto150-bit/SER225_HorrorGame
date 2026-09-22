package GameObject;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;


public class Book {

    private BufferedImage image;
    private boolean isActive;
    private Key interactKey = Key.C; // key to interact with the book
    private KeyLocker keyLocker = new KeyLocker();



    public Book() {

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/Resources/Book.png"));

        } catch (IOException e) {
            e.printStackTrace();
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
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void draw(GraphicsHandler graphicsHandler){
        if (isActive && image != null) {
            graphicsHandler.drawImage(image, -155, 50, 1100,480); // adjust position/size as needed
        }

    }


}

    


