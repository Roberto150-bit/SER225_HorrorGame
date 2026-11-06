package EnhancedMapTiles;

import Builders.FrameBuilder;
import Enemies.NecromancerBoss;
import Engine.GraphicsHandler;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Random;

// The light of the Necromancer's ritual: the eight candle stands around the room and the pentagram painted on the floor.
// It has no picture of its own (the candles and pentagram are part of the room's art), it only draws their light over the darkness,
// and the pentagram's lines burn brighter while the Necromancer is awake.
// The fire burns low while the Necromancer waits, flares when it wakes, and gutters out once it's destroyed.
public class RitualFire extends EnhancedMapTile {
    // where each candle's flame is in the room (the tops of the candle stands in SecretRoomTileset.png)
    private static final float[][] CANDLES = {
        {222, 217}, {350, 217}, {158, 281}, {414, 281},
        {158, 409}, {414, 409}, {222, 473}, {350, 473}
    };
    // the pentagram on the floor (its centre and radius match SecretRoomTileset.png, at the room's 2x scale)
    private static final float CIRCLE_X = 287;
    private static final float CIRCLE_Y = 383;
    private static final float CIRCLE_RADIUS = 150;
    private static final float PENTAGRAM_RADIUS = 88;

    private static final Color FLAME = new Color(255, 140, 70);
    private static final Color CIRCLE = new Color(230, 40, 30);

    private final NecromancerBoss boss;
    private final Random random = new Random();
    private final float[] flicker = new float[CANDLES.length];
    private int timer = 0;
    // 0 = burning low, 1 = full, eases toward its target so changes aren't sudden
    private float intensity = 0.45f;

    public RitualFire(NecromancerBoss boss) {
        super(0, 0, new SpriteSheet(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), 16, 16), TileType.PASSABLE);
        this.boss = boss;
        setIsUncollidable(true);
        // it lights the whole room, so it's kept updating wherever the camera is
        isUpdateOffScreen = true;
    }

    @Override
    public void update(Player player) {
        super.update(player);
        timer++;
        for (int i = 0; i < flicker.length; i++) {
            // each flame wavers on its own
            flicker[i] = 0.85f + 0.1f * (float) Math.sin((timer + i * 37) / 5.0) + 0.08f * random.nextFloat();
        }
        float target;
        if (boss.isDefeated()) {
            target = 0.15f;
        } else if (boss.isAwake()) {
            target = boss.isEnraged() ? 1.15f : 1f;
        } else {
            target = 0.45f;
        }
        intensity += (target - intensity) * 0.03f;
    }

    // nothing to draw in the world itself (the camera draws enhanced map tiles through drawBottomLayer)
    @Override
    public void drawBottomLayer(GraphicsHandler graphicsHandler) { }

    @Override
    public void drawAboveDarkness(GraphicsHandler graphicsHandler) {
        Graphics2D g = graphicsHandler.getGraphics();
        float cameraX = map.getCamera().getX();
        float cameraY = map.getCamera().getY();

        float circlePulse = 0.35f + 0.1f * (float) Math.sin(timer / 25.0);
        glow(g, CIRCLE_X - cameraX, CIRCLE_Y - cameraY, CIRCLE_RADIUS, CIRCLE, circlePulse * intensity);
        drawBurningPentagram(g, CIRCLE_X - cameraX, CIRCLE_Y - cameraY);

        for (int i = 0; i < CANDLES.length; i++) {
            float x = CANDLES[i][0] - cameraX;
            float y = CANDLES[i][1] - cameraY;
            float strength = Math.min(1f, flicker[i] * intensity);
            glow(g, x, y, 46 * Math.max(0.5f, intensity), FLAME, strength * 0.8f);
            // the flame itself
            glow(g, x, y - 3, 7, new Color(255, 230, 170), strength);
        }
    }

    // the painted pentagram's lines, lit up like embers (strongest during the fight)
    private void drawBurningPentagram(Graphics2D g, float centerX, float centerY) {
        float strength = Math.max(0f, Math.min(1f, (intensity - 0.4f) * 1.2f));
        if (strength <= 0) {
            return;
        }
        float flicker = 0.8f + 0.2f * (float) Math.sin(timer / 9.0);
        int alpha = Math.round(170 * strength * flicker);
        java.awt.Stroke oldStroke = g.getStroke();
        Object oldAntialias = g.getRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        float[][] points = new float[5][2];
        for (int k = 0; k < 5; k++) {
            double angle = Math.toRadians(90 + 72 * k); // inverted: one point straight down
            points[k][0] = centerX + PENTAGRAM_RADIUS * (float) Math.cos(angle);
            points[k][1] = centerY + PENTAGRAM_RADIUS * (float) Math.sin(angle);
        }
        // a wide dim stroke under a thin bright one
        float[] widths = {7f, 2.5f};
        int[] alphas = {alpha / 3, alpha};
        Color[] colors = {new Color(255, 40, 30), new Color(255, 120, 80)};
        for (int pass = 0; pass < 2; pass++) {
            g.setStroke(new java.awt.BasicStroke(widths[pass], java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g.setColor(new Color(colors[pass].getRed(), colors[pass].getGreen(), colors[pass].getBlue(), alphas[pass]));
            g.draw(new Ellipse2D.Float(centerX - PENTAGRAM_RADIUS, centerY - PENTAGRAM_RADIUS, PENTAGRAM_RADIUS * 2, PENTAGRAM_RADIUS * 2));
            for (int k = 0; k < 5; k++) {
                float[] a = points[k];
                float[] b = points[(k + 2) % 5];
                g.draw(new java.awt.geom.Line2D.Float(a[0], a[1], b[0], b[1]));
            }
        }
        g.setStroke(oldStroke);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, oldAntialias);
    }

    // a soft round glow that fades out from the centre
    public static void glow(Graphics2D g, float centerX, float centerY, float radius, Color color, float strength) {
        strength = Math.max(0f, Math.min(1f, strength));
        if (strength <= 0 || radius <= 0) {
            return;
        }
        Color inner = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(150 * strength));
        Color outer = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        g.setPaint(new RadialGradientPaint(centerX, centerY, radius, new float[] {0f, 1f}, new Color[] {inner, outer}));
        g.fill(new Ellipse2D.Float(centerX - radius, centerY - radius, radius * 2, radius * 2));
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0, 0)).build();
        return new GameObject(x, y, frame);
    }

}
