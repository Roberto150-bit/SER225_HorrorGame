package NPCs;

import Builders.FrameBuilder;
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import GameObject.SpriteSheet;
import Level.NPC;
import Utils.Point;

import java.util.HashMap;

// This class is for the Mrs. Vale NPC, the Housekeeper
// she is stationary, so she only uses her idle animation
// MsVale.png is generated from NPCs/MsVale/Character_4_Sheet.png the same way Dean.png is (32x32 frames, 1px gaps, magenta background)
// sprite sheet rows: 0 AttackPrick, 1 AttackMagic, 2 AttackShoot, 3 AttackSlash, 4 Death, 5 Hit, 6 Jump, 7 Run, 8 Idle
// the art faces left, so the RIGHT animations are the flipped ones
public class MsValeNPC extends NPC {

    public MsValeNPC(int id, Point location) {
        super(id, location.x, location.y, new SpriteSheet(ImageLoader.load("MsVale.png"), 32, 32), "STAND_RIGHT");
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
        }};
    }

    @Override
    public void draw(GraphicsHandler graphicsHandler) {
        super.draw(graphicsHandler);
    }
}
