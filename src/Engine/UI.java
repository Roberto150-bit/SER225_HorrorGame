package Engine;

import GameObject.Book;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UI {
    private Font secretSolverFont;
    private Book book;

    public UI (Book book) {
        this.book = book;
        try {
            Font secretSolver = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("src/Resources/secret_solver.ttf"))).deriveFont(Font.PLAIN, 30f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(secretSolver);
            secretSolverFont = secretSolver;
        } catch (IOException | FontFormatException e) {
            System.out.println("ERROR: BOOK TEXT FONT NOT FOUND");
        }
    }

    public void draw(GraphicsHandler graphicsHandler) {
        if (book.isActive()) {            
            graphicsHandler.drawString("WASD to Move \nSPACE to Continue\nE to Interact \nC to Open/Close Journal", 120,100, secretSolverFont, Color.black);
            
        }
    }

}
