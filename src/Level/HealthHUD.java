package Level;

import Engine.GraphicsHandler;
import Engine.ScreenManager;
import java.awt.Color;
import java.awt.Graphics2D;

// flashes the screen red when the player loses a heart
public class HealthHUD {

    private static final int MAX_FLASH_ALPHA = 110;

    public void draw(GraphicsHandler graphicsHandler, Player player) {
        if (player.isHurt()) {
            Graphics2D g = graphicsHandler.getGraphics();
            int alpha = Math.round(MAX_FLASH_ALPHA * player.getHurtProgress());
            g.setColor(new Color(150, 0, 0, alpha));
            g.fillRect(0, 0, ScreenManager.getScreenWidth(), ScreenManager.getScreenHeight());
        }
    }
}
