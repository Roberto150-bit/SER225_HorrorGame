package GameObject;

import Engine.GraphicsHandler;
import Engine.SoundPlayer;
import Level.Camera;
import Level.Map;
import Level.MapTile;
import Level.NPC;
import Level.Player;
import Level.TileType;
import Utils.Point;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

// relic thrown the way the player is aiming, does 3 damage to the first enemy it hits
// missing still uses up the relic
public class ThrownItem {

    public static final int DAMAGE = 3;
    private static final float SPEED = 7f; // pixels per frame
    private static final float MAX_DISTANCE = 320f; // max distance
    private static final int HITBOX_SIZE = 24;
    private static final int DRAW_SIZE = 32;
    private static final float SPIN_SPEED = 0.35f; // radians per frame

    public static final String THROW_SOUND = "DefensiveItemUse.wav";
    private static final String[] HIT_SOUNDS = {
            "knife_free/WEAPKnif_Knife hit, Impact, 01.wav",
            "knife_free/WEAPKnif_Knife hit, Impact, 02.wav",
            "knife_free/WEAPKnif_Knife hit, Impact, 03.wav"
    };
    private static final float HIT_BURST_RADIUS = 90f;
    private static final float MISS_BURST_RADIUS = 30f;

    private BufferedImage image;
    private float x, y;
    private float directionX, directionY;
    private float distanceTravelled = 0;
    private float spin = 0;
    private boolean isFinished = false;

    public ThrownItem(BufferedImage image, Player player) {
        this.image = image;
        Point start = player.getCenter();
        Point aim = player.getAimDirection();
        this.x = start.x;
        this.y = start.y;
        this.directionX = aim.x;
        this.directionY = aim.y;
        SoundPlayer.play(THROW_SOUND);
    }

    // moves the item and checks for hits
    public void update(Map map, DefensiveItemEffect burst) {
        if (isFinished) {
            return;
        }
        x += directionX * SPEED;
        y += directionY * SPEED;
        distanceTravelled += SPEED;
        spin += SPIN_SPEED;

        Rectangle hitbox = new Rectangle(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f, HITBOX_SIZE, HITBOX_SIZE);
        for (NPC npc : map.getActiveNPCs()) {
            if (npc.isEnemy() && npc.getBounds().intersects(hitbox)) {
                npc.hitByDefensiveItem(DAMAGE);
                SoundPlayer.playRandom(HIT_SOUNDS);
                burst.start(new Point(x, y), HIT_BURST_RADIUS);
                isFinished = true;
                return;
            }
        }

        MapTile tile = map.getTileByPosition(x, y);
        boolean hitWall = tile == null || tile.getTileType() == TileType.NOT_PASSABLE;
        if (hitWall || distanceTravelled >= MAX_DISTANCE) {
            burst.start(new Point(x, y), MISS_BURST_RADIUS);
            isFinished = true;
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void draw(GraphicsHandler graphicsHandler, Camera camera) {
        if (isFinished) {
            return;
        }
        Graphics2D g = graphicsHandler.getGraphics();
        AffineTransform oldTransform = g.getTransform();
        Object oldAntialiasing = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(camera.getZoom(), camera.getZoom()); // match the camera zoom

        float screenX = x - camera.getX();
        float screenY = y - camera.getY();

        g.setColor(new Color(255, 235, 160, 90));
        g.fill(new Ellipse2D.Float(screenX - DRAW_SIZE * 0.75f, screenY - DRAW_SIZE * 0.75f, DRAW_SIZE * 1.5f, DRAW_SIZE * 1.5f));

        g.translate(screenX, screenY);
        g.rotate(spin);
        g.drawImage(image, -DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE, null);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
        g.setTransform(oldTransform);
    }
}
