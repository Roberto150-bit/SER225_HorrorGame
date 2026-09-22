package Engine;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
    
    private static int mouseX;
    private static int mouseY;
    private static boolean leftClicked;

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override 
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClicked = true;
        }
    }

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static boolean isLeftClicked() {
        return leftClicked;
    }

    public static void resetClick() {
        leftClicked = false;
    }
}
