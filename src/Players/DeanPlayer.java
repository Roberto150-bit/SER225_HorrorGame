package Players;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.Player;

import java.util.HashMap;

// This is the class for the Dean player character
// basically just sets some values for physics and then defines animations
// Dean.png is generated from Players/Dean/Character_2_Sheet.png (32x32 frames, 1px gaps, magenta background)
// sprite sheet rows: 0 AttackPrick, 1 AttackMagic, 2 AttackShoot, 3 AttackSlash, 4 Death, 5 Hit, 6 Jump, 7 Run, 8 Idle,
// 9 Walk Down, 10 Stand Up (back view), 11 Walk Up
// the art faces left, so the RIGHT animations are the flipped ones
public class DeanPlayer extends Player {

    public DeanPlayer(float x, float y) {
        super(new SpriteSheet(ImageLoader.load("Dean.png"), 32, 32), x, y, "STAND_RIGHT");
        walkSpeed = 2.3f;
    }

    public void update() {
        super.update();
    }

    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }

    @Override
    public HashMap<String, Frame[]> loadAnimations(SpriteSheet spriteSheet) {
        return new HashMap<String, Frame[]>() {{
            put("STAND_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(8, 0), 30)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 1), 15)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 2), 15)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 3), 15)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("STAND_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(8, 0), 30)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 1), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 2), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 3), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("WALK_RIGHT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(7, 0), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 1), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 2), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 3), 8)
                            .withScale(3)
                            .withImageEffect(ImageEffect.FLIP_HORIZONTAL)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("WALK_LEFT", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(7, 0), 8)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 1), 8)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 2), 8)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(7, 3), 8)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            // top-down animations (rows 9-11 of Dean.png, generated from the idle frames)
            // the original art already faces the camera, so STAND_DOWN reuses the idle row
            put("STAND_DOWN", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(8, 0), 30)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 1), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 2), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(8, 3), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("WALK_DOWN", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(9, 0), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(9, 1), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(9, 2), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(9, 3), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("STAND_UP", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(10, 0), 30)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(10, 1), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(10, 2), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(10, 3), 15)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });

            put("WALK_UP", new Frame[] {
                    new FrameBuilder(spriteSheet.getSprite(11, 0), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(11, 1), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(11, 2), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build(),
                    new FrameBuilder(spriteSheet.getSprite(11, 3), 10)
                            .withScale(3)
                            .withBounds(9, 23, 13, 7)
                            .build()
            });
        }};
    }
}
