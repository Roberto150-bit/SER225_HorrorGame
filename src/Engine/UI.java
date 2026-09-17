package Engine;

import GameObject.Book;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class UI {
    private GamePanel gp;
    private Font arial_40;
    private BufferedImage bookImage;


public UI (GamePanel gp) {
    this.gp = gp;
    this.arial_40 = new Font("Arial", Font.PLAIN, 20);

    Book bookObj = new Book();
    bookImage = bookObj.getImage();
}

public void draw(Graphics2D g2) {
    g2.setFont(arial_40);
    g2.setColor(Color.black);
    g2.drawImage(bookImage, -100,50,1000,500,null);
    g2.drawString("Spooky Book", 140,100);
}

}
