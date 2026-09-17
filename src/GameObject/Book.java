package GameObject;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Book {

    private BufferedImage image;

    public Book() {
        String name = "Book";

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/Resources/Book.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage() {
        return image;
    }

}
