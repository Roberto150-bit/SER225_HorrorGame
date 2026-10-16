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

    private int currentPage = 0;


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

    public void IncreaseNumPages() {
        this.currentPage++;
    }

    public void draw(GraphicsHandler graphicsHandler) {
        if (book.isActive()) {
            if (currentPage == 0) {            
                graphicsHandler.drawString("WASD to Move \nSPACE to Continue\nE to Interact \nC to Open/Close Journal", 115,100, secretSolverFont, Color.black);
            }
            else if (currentPage == 1) {
                graphicsHandler.drawString("I am sorry for the souls \n who rest here. \n \n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "It’s my fault\t  It’s my fault\r\n" + //
                                        "       ", 115,120, secretSolverFont, Color.black);
            } else if (currentPage == 2) {
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
            } else if (currentPage == 3) {
                graphicsHandler.drawString("“It” created something \nnew. Why?\r\n" + //
                                        "\r\n" + //
                                        "What kind of crazy project…\r\n" + //
                                        "\r\n" + //
                                        "It’s too late now\r\n" + //
                                        "\r\n" + //
                                        "If I was more decisive..\r\n" + //
                                        "", 110,120, secretSolverFont, Color.black);
            } else if (currentPage == 4) {
                graphicsHandler.drawString("I saw it morph, it stood \nthere\r\n" + //
                                        "\r\n" + //
                                        "In the middle of the room\r\n" + //
                                        "\r\n" + //
                                        "Strong winds around it\r\n" + //
                                        "\r\n" + //
                                        "Just what.. Is that thing…\r\n" + //
                                        " ", 115,120, secretSolverFont, Color.black);
            } else if (currentPage == 5) {
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
            } else {
                graphicsHandler.drawString("Error, Current Page: " + currentPage, 120,100, secretSolverFont, Color.black);
            }
        }
    }


}
