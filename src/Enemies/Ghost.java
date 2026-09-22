package Enemies;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Level.Player;
import Utils.Point;

import java.util.HashMap;

// A ghost enemy that slowly floats toward the player, but keeps a short gap instead of overlapping them
public class Ghost extends NPC {
    private float speed = 1.4f;
    private float followDistance = 60f;

    public Ghost(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("Ghost.png"), 16, 22), "STAND_LEFT");
    }

    @Override
    public void performAction(Player player) {
        float dx = (player.getBounds().getX() + player.getBounds().getWidth() / 2f) - (getBounds().getX() + getBounds().getWidth() / 2f);
        float dy = (player.getBounds().getY() + player.getBounds().getHeight() / 2f) - (getBounds().getY() + getBounds().getHeight() / 2f);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > followDistance) {
            if (dx < 0) {
                currentAnimationName = "WALK_LEFT";
            } else if (dx > 0) {
                currentAnimationName = "WALK_RIGHT";
            }
            if (Math.abs(dx) > 1) {
                moveXHandleCollision(Math.signum(dx) * speed);
            }
            if (Math.abs(dy) > 1) {
                moveYHandleCollision(Math.signum(dy) * speed);
            }
        } else {
            currentAnimationName = currentAnimationName.contains("RIGHT") ? "STAND_RIGHT" : "STAND_LEFT";
        }
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(1, 1, 14, 16)
                            .build()
            });
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(1, 1, 14, 16)
                            .build()
            });
            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(0, 0))
                            .withScale(3)
                            .withBounds(1, 1, 14, 16)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .build()
            });
        }};
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
