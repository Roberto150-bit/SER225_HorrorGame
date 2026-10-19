package GameObject;

import Engine.GraphicsHandler;
import Engine.SoundPlayer;
import Level.Camera;
import Level.Map;
import Level.NPC;
import Level.Player;
import Utils.Point;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;

// knife swing in front of the player, does 1 damage
public class KnifeSlash {

    public static final int DAMAGE = 1;
    private static final float REACH = 75f; // reach in pixels
    private static final float ARC_DEGREES = 120f; // how wide the swing is
    private static final int DURATION = 12; // frames the swing lasts

    // sounds from Resources/Audios/knife_free
    public static final String[] PICKUP_SOUNDS = {
            "knife_free/WEAPKnif_Knife, Unsheathe, 01.wav",
            "knife_free/WEAPKnif_Knife, Unsheathe, 02.wav",
            "knife_free/WEAPKnif_Knife, Unsheathe, 03.wav"
    };
    private static final String[] SWING_SOUNDS = {
            "knife_free/WEAPKnif_Knife hit, Slice, Cut, 01.wav",
            "knife_free/WEAPKnif_Knife hit, Slice, Cut, 02.wav",
            "knife_free/WEAPKnif_Knife hit, Slice, Cut, 03.wav",
            "knife_free/WEAPKnif_Knife hit, Slice, Cut, 04.wav",
            "knife_free/WEAPKnif_Knife hit, Slice, Cut, 05.wav"
    };
    private static final String[] HIT_SOUNDS = {
            "knife_free/WEAPKnif_Knife hit, Stab, 01.wav",
            "knife_free/WEAPKnif_Knife hit, Stab, 02.wav",
            "knife_free/WEAPKnif_Knife hit, Stab, 03.wav",
            "knife_free/WEAPKnif_Knife hit, Stab, 04.wav"
    };

    private Point center;
    private Point aim;
    private int timer = 0;

    // hits every enemy in front of the player within reach
    public void swing(Player player, Map map) {
        center = player.getCenter();
        aim = player.getAimDirection();
        timer = DURATION;

        boolean hitSomething = false;
        for (NPC npc : map.getActiveNPCs()) {
            if (npc.isEnemy() && isInSwing(npc)) {
                npc.hitByDefensiveItem(DAMAGE);
                hitSomething = true;
            }
        }
        SoundPlayer.playRandom(hitSomething ? HIT_SOUNDS : SWING_SOUNDS);
    }

    // true if the enemy is close enough and in front of the player
    private boolean isInSwing(NPC npc) {
        float dx = npc.getBounds().getX() + npc.getBounds().getWidth() / 2f - center.x;
        float dy = npc.getBounds().getY() + npc.getBounds().getHeight() / 2f - center.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float enemySize = Math.max(npc.getBounds().getWidth(), npc.getBounds().getHeight()) / 2f;
        if (distance > REACH + enemySize) {
            return false;
        }
        if (distance < enemySize) {
            return true;
        }
        // angle between the aim and the enemy
        float angleToEnemy = (float) Math.toDegrees(Math.acos((dx * aim.x + dy * aim.y) / distance));
        return angleToEnemy <= ARC_DEGREES / 2f;
    }

    public void update() {
        if (timer > 0) {
            timer--;
        }
    }

    public boolean isActive() {
        return timer > 0;
    }

    // draws the swing arc
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
        float x = center.x - camera.getX();
        float y = center.y - camera.getY();

        // arc angles go counter-clockwise, so y is flipped
        float aimAngle = (float) Math.toDegrees(Math.atan2(-aim.y, aim.x));
        float sweptDegrees = ARC_DEGREES * Math.min(1f, progress * 2f);
        float startAngle = aimAngle + ARC_DEGREES / 2f - sweptDegrees;
        int alpha = Math.round(220 * (1f - progress));

        Arc2D.Float arc = new Arc2D.Float(x - REACH, y - REACH, REACH * 2, REACH * 2, startAngle, sweptDegrees, Arc2D.OPEN);
        g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(200, 210, 225, alpha / 2));
        g.draw(arc);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, alpha));
        g.draw(arc);

        g.setStroke(new BasicStroke(1));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
        g.setTransform(oldTransform);
    }
}
