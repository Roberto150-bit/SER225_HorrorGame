package EnhancedMapTiles;

import java.awt.image.BufferedImage;
import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;


public class CollectibleObject extends EnhancedMapTile {
    
    private BufferedImage itemImage;
    private BufferedImage[] gifFrames; // only set for animated (gif) items
    private int gifFrameDelay;

    public CollectibleObject(Point location) {
        this(location, "Rock.png", 14);
    }

    // imageFile is a square image of imageSize pixels
    public CollectibleObject(Point location, String imageFile, int imageSize) {
        super(location.x, location.y, new SpriteSheet(loadImage(imageFile), imageSize, imageSize), TileType.PASSABLE);
        this.itemImage = loadImage(imageFile);
        // gifs are static on the ground but animate once in the hotbar
        if (imageFile.toLowerCase().endsWith(".gif")) {
            gifFrames = ImageLoader.loadGifFrames(imageFile);
            gifFrameDelay = Math.max(20, ImageLoader.getGifFrameDelay(imageFile) / 2); // played at double speed
        }
    }

    // images with their own alpha channel keep it, others use the magenta transparent color
    private static BufferedImage loadImage(String imageFile) {
        String name = imageFile.toLowerCase();
        return name.endsWith(".png") && !imageFile.equals("Rock.png") || name.endsWith(".gif")
                ? ImageLoader.loadWithAlpha(imageFile)
                : ImageLoader.load(imageFile);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (player.touching(this)) {

            boolean addedToHotbar = gifFrames != null
                    ? map.getHotbarUI().addAnimatedItem(gifFrames, gifFrameDelay)
                    : map.getHotbarUI().addItem(itemImage);

            if (addedToHotbar) {
                player.collectItem();
                setMapEntityStatus(MapEntityStatus.REMOVED);
            }
            
        }
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {

        BufferedImage image = spriteSheet.getSubImage(0,0);
        // large images are shrunk to 32px in the world, small ones (like the rock) are doubled
        float scale = image.getWidth() > 32 ? 32f / image.getWidth() : 2;
        Frame frame = new FrameBuilder(image).withScale(scale).build();

        return new GameObject(x, y, frame);
    }
}
