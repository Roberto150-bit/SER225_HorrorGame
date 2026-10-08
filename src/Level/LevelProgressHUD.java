
package Level;

import Engine.GraphicsHandler;
import java.awt.Color;
import java.awt.Font;

public class LevelProgressHUD {

    private static final int X = 20;
    private static final int Y = 20;
    private static final int WIDTH = 180;
    private static final int HEIGHT = 12;

    // Draw the player's current level and XP progress.
    public void draw(GraphicsHandler graphicsHandler) {

        int level = PlayerProgressManager.getLevel();
        int currentXP = PlayerProgressManager.getCurrentXP();
        int requiredXP = PlayerProgressManager.getXPRequired();

        double progress = PlayerProgressManager.getXPPercentage();

        // Display current level.
        graphicsHandler.drawString(
            "LEVEL " + level,
            X, Y + 15,
            new Font("Serif", Font.BOLD, 18),
            Color.WHITE
        );

        // Display current XP.
        graphicsHandler.drawString(
            currentXP + "/" + requiredXP + " XP",
            X + 105, Y + 15,
            new Font("Arial", Font.PLAIN, 13),
            Color.LIGHT_GRAY
        );

        // Draw the empty progress bar.
        graphicsHandler.drawFilledRectangle(
            X, Y + 25, WIDTH, HEIGHT,
            new Color(50, 40, 55)
        );

        // Calculate and draw the filled portion.
        int filledWidth = (int) (WIDTH * progress / 100.0);

        graphicsHandler.drawFilledRectangle(
            X, Y + 25, filledWidth, HEIGHT,
            new Color(160, 110, 190)
        );

        // Draw the border around the bar.
        graphicsHandler.drawRectangle(
            X, Y + 25, WIDTH, HEIGHT,
            new Color(195, 165, 205)
        );
    }
}
