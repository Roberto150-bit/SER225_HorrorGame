package Engine;

import GameObject.Book;
import java.awt.Color;
import java.awt.Font;

public class UI {
    private Font arial_40;
    private Book book;

    public UI (Book book) {
        this.book = book;
        this.arial_40 = new Font("Chalkduster", Font.PLAIN, 20);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        if (book.isActive()) {            
            graphicsHandler.drawString("A spooky book", 140,100, arial_40, Color.black);
        }
    }

}
