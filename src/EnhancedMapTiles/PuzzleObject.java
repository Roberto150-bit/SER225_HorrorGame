
package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.TileType;
import Puzzles.PuzzleConfig;
import Scripts.PuzzleScript;
import Utils.Point;
import java.awt.image.BufferedImage;

public class PuzzleObject extends EnhancedMapTile {

    // Existing constructor: keeps the original rock.
    public PuzzleObject(Point location) {
        super(
            location.x,
            location.y,
            new SpriteSheet(
                ImageLoader.load("Rock.png"),
                16,
                16
            ),
            TileType.NOT_PASSABLE
        );
    }

    // Automatically chooses the sprite and interaction.
    public PuzzleObject(Point location, PuzzleConfig config) {
        super(
            location.x,
            location.y,
            createSpriteSheet(config),
            TileType.NOT_PASSABLE
        );

        setInteractScript(new PuzzleScript(config));
    }


    private static SpriteSheet createSpriteSheet(PuzzleConfig config) {

        BufferedImage original = ImageLoader.loadWithAlpha(
            getSpriteName(config)
        );

        // The existing FrameBuilder scales sprites by 3.
        // A 16x16 image therefore appears as 48x48 in-game.
        int size = 16;

        BufferedImage resized = new BufferedImage(
            size,
            size,
            BufferedImage.TYPE_INT_ARGB
        );

        java.awt.Graphics2D g = resized.createGraphics();

        g.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        // Preserve proportions rather than stretching sprites.
        double scale = Math.min(
            (double) size / original.getWidth(),
            (double) size / original.getHeight()
        );

        int width = Math.max(1,
            (int) Math.round(original.getWidth() * scale));

        int height = Math.max(1,
            (int) Math.round(original.getHeight() * scale));

        int x = (size - width) / 2;
        int y = (size - height) / 2;

        g.drawImage(original, x, y, width, height, null);
        g.dispose();

        return new SpriteSheet(resized, size, size);
    }


    private static String getSpriteName(PuzzleConfig config) {
        switch (config.getPuzzleType()) {
            case SYMBOL_LOCK:
                return "Puzzles/SymbolLock/PuzzleLockbox.png";

            case TUNE_RADIO:
                return "Puzzles/TuneRadio/PuzzleRadio.png";

            case HAUNTED_CLOCK:
                return "Puzzles/HauntedClock/PuzzleClock.png";

            case RITUAL_SEQUENCE:
                return "Puzzles/RitualSequence/PuzzleAltar.png";

            case SPIRIT_MIRROR:
                return "Puzzles/SpiritMirror/PuzzleMirror.png";

            // the final seal reuses the lockbox until it gets its own sprite
            case CODE_LOCK:
                return "Puzzles/SymbolLock/PuzzleLockbox.png";

            default:
                throw new IllegalArgumentException(
                    "Unsupported puzzle type: " + config.getPuzzleType()
                );
        }
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {

        BufferedImage image = spriteSheet.getSubImage(0, 0);

        // Original 16x16 rocks use scale 3.
        // New 48x48 puzzle sprites use scale 1.
        int scale = image.getWidth() == 16 ? 3 : 1;

        Frame frame = new FrameBuilder(image)
            .withScale(scale)
            .build();

        return new GameObject(x, y, frame);
    }
}
