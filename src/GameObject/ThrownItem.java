package GameObject;

import Engine.GraphicsHandler;
import Engine.ImageLoader;
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
// also used for the bow's arrows (see arrow()), which fly faster and further, don't spin, and do 1 damage
public class ThrownItem {

    public static final int DAMAGE = 3;
    private static final float SPEED = 7f; // pixels per frame
    private static final float MAX_DISTANCE = 320f; // max distance
    private static final int HITBOX_SIZE = 24;
    private static final int DRAW_SIZE = 32;
    private static final float SPIN_SPEED = 0.35f; // radians per frame

    public static final int ARROW_DAMAGE = 1;
    private static final float ARROW_SPEED = 11f;
    private static final float ARROW_MAX_DISTANCE = 450f;
    public static final String ARROW_SOUND = "knife_free/WEAPKnif_Knife, Unsheathe, 02.wav";
    private static BufferedImage arrowImage;

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

    // these differ between thrown relics and arrows
    private int damage = DAMAGE;
    private float speed = SPEED;
    private float maxDistance = MAX_DISTANCE;
    private boolean isArrow = false;

    public ThrownItem(BufferedImage image, Player player) {
        this(image, player, THROW_SOUND);
    }

    private ThrownItem(BufferedImage image, Player player, String sound) {
        this.image = image;
        Point start = player.getCenter();
        Point aim = player.getAimDirection();
        this.x = start.x;
        this.y = start.y;
        this.directionX = aim.x;
        this.directionY = aim.y;
        SoundPlayer.play(sound);
    }

    // an arrow fired from the bow
    public static ThrownItem arrow(Player player) {
        if (arrowImage == null) {
            arrowImage = ImageLoader.loadWithAlpha("Weapons/arrow.png");
        }
        ThrownItem arrow = new ThrownItem(arrowImage, player, ARROW_SOUND);
        arrow.damage = ARROW_DAMAGE;
        arrow.speed = ARROW_SPEED;
        arrow.maxDistance = ARROW_MAX_DISTANCE;
        arrow.isArrow = true;
        return arrow;
    }

    // moves the item and checks for hits
    public void update(Map map, DefensiveItemEffect burst) {
        if (isFinished) {
            return;
        }
        x += directionX * speed;
        y += directionY * speed;
        distanceTravelled += speed;
        spin += SPIN_SPEED;

        Rectangle hitbox = new Rectangle(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f, HITBOX_SIZE, HITBOX_SIZE);
        for (NPC npc : map.getActiveNPCs()) {
            if (npc.isEnemy() && npc.getBounds().intersects(hitbox)) {
                npc.hitByDefensiveItem(damage);
                SoundPlayer.playRandom(HIT_SOUNDS);
                if (!isArrow) {
                    burst.start(new Point(x, y), HIT_BURST_RADIUS);
                }
                isFinished = true;
                return;
            }
        }

        MapTile tile = map.getTileByPosition(x, y);
        boolean hitWall = tile == null || tile.getTileType() == TileType.NOT_PASSABLE;
        if (hitWall || distanceTravelled >= maxDistance) {
            if (!isArrow) {
                burst.start(new Point(x, y), MISS_BURST_RADIUS);
            }
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

        if (!isArrow) {
            g.setColor(new Color(255, 235, 160, 90));
            g.fill(new Ellipse2D.Float(screenX - DRAW_SIZE * 0.75f, screenY - DRAW_SIZE * 0.75f, DRAW_SIZE * 1.5f, DRAW_SIZE * 1.5f));
        }

        g.translate(screenX, screenY);
        // relics tumble, arrows point where they're flying (the arrow image points up-right, so turn it by 45 degrees)
        g.rotate(isArrow ? Math.atan2(directionY, directionX) + Math.PI / 4 : spin);
        g.drawImage(image, -DRAW_SIZE / 2, -DRAW_SIZE / 2, DRAW_SIZE, DRAW_SIZE, null);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
        g.setTransform(oldTransform);
    }
}
