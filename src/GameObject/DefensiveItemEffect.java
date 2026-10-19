package GameObject;

import Engine.GraphicsHandler;
import Level.Camera;
import Utils.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

// light burst where a thrown relic lands
public class DefensiveItemEffect {

    private static final int DURATION = 35; // frames the effect lasts
    private static final Color LIGHT_COLOR = new Color(255, 235, 160);

    private float centerX, centerY;
    private float maxRadius;
    private int timer = 0;

    public void start(Point center, float maxRadius) {
        centerX = center.x;
        centerY = center.y;
        this.maxRadius = maxRadius;
        timer = DURATION;
    }

    public void update() {
        if (timer > 0) {
            timer--;
        }
    }

    public boolean isActive() {
        return timer > 0;
    }

    public void draw(GraphicsHandler graphicsHandler, Camera camera) {
        if (!isActive()) {
            return;
        }
        Graphics2D g = graphicsHandler.getGraphics();
        AffineTransform oldTransform = g.getTransform();
        Object oldAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(camera.getZoom(), camera.getZoom()); // match the camera zoom

        float progress = 1f - timer / (float) DURATION;
        float easedProgress = 1f - (1f - progress) * (1f - progress); // ease out
        float radius = maxRadius * easedProgress;
        float fade = 1f - progress;
        float x = centerX - camera.getX();
        float y = centerY - camera.getY();

        // glow
        g.setColor(withAlpha(LIGHT_COLOR, 70 * fade));
        g.fill(new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2));

        // outer and inner rings
        g.setStroke(new BasicStroke(6f * fade + 1f));
        g.setColor(withAlpha(LIGHT_COLOR, 230 * fade));
        g.draw(new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2));
        float innerRadius = radius * 0.7f;
        g.setStroke(new BasicStroke(2f));
        g.setColor(withAlpha(Color.WHITE, 160 * fade));
        g.draw(new Ellipse2D.Float(x - innerRadius, y - innerRadius, innerRadius * 2, innerRadius * 2));

        g.setStroke(new BasicStroke(1));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
        g.setTransform(oldTransform);
    }

    private static Color withAlpha(Color color, float alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, Math.round(alpha))));
    }
}
