package Lighting;

import Engine.GraphicsHandler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

public class DarknessEffect {

    // How dark the outside of the visible area becomes.
    // 0 = completely transparent, 255 = completely black.
    private static final int MAX_DARKNESS_ALPHA = 235;

    public void draw(
            GraphicsHandler graphicsHandler,
            float centerX,
            float centerY,
            float radius
    ) {
        Graphics2D graphics = graphicsHandler.getGraphics();

        // Do not draw anything if there is no valid radius.
        if (radius <= 0) {
            return;
        }

        // Save the current graphics settings so this effect does not
        // change how anything else in the game is drawn.
        Paint originalPaint = graphics.getPaint();
        RenderingHints originalHints = graphics.getRenderingHints();

        // Makes the circular fade smoother.
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // The gradient begins at the player's screen position.
        Point2D center = new Point2D.Float(centerX, centerY);

        // Defines where the darkness changes.
        float[] distances = {
                0.0f,
                0.55f,
                0.80f,
                1.0f
        };

        // Center stays visible and gradually becomes darker toward the edge.
        Color[] colors = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 120),
                new Color(0, 0, 0, MAX_DARKNESS_ALPHA)
        };

        RadialGradientPaint darknessGradient = new RadialGradientPaint(
                center,
                radius,
                distances,
                colors
        );

        graphics.setPaint(darknessGradient);

        // Cover the entire currently visible game screen.
        java.awt.Rectangle screenBounds = graphics.getClipBounds();

        if (screenBounds != null) {
            graphics.fillRect(
                    screenBounds.x,
                    screenBounds.y,
                    screenBounds.width,
                    screenBounds.height
            );
        }

        // Restore the graphics settings so this class does not affect
        // anything drawn afterward.
        graphics.setPaint(originalPaint);
        graphics.setRenderingHints(originalHints);
    }
}