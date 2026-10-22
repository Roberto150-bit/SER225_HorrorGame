package EnhancedMapTiles;

import java.awt.image.BufferedImage;
import Builders.FrameBuilder;
import Engine.ImageLoader;
import Engine.ItemUse;
import Engine.SoundPlayer;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;


public class CollectibleObject extends EnhancedMapTile {
    
    private BufferedImage itemImage;
    private BufferedImage[] gifFrames; // only set for animated (gif) items
    private int gifFrameDelay;
    private ItemUse itemUse;
    private String[] pickupSounds; // optional sounds played on pickup

    // imageFile is a square image of imageSize pixels
    public CollectibleObject(Point location, String imageFile, int imageSize) {
        this(location, imageFile, imageSize, ItemUse.NONE);
    }

    // itemUse decides what happens when the item is used from the hotbar (see ItemUse)
    public CollectibleObject(Point location, String imageFile, int imageSize, ItemUse itemUse) {
        super(location.x, location.y, new SpriteSheet(loadImage(imageFile), imageSize, imageSize), TileType.PASSABLE);
        this.itemImage = loadImage(imageFile);
        this.itemUse = itemUse;
        // picked up by walking over it, so it never blocks movement -- otherwise an item dropped on top of the player
        // (like a ghost's relic) would shove them to the far side of it, possibly into a wall
        setIsUncollidable(true);
        // gifs are static on the ground but animate once in the hotbar
        if (imageFile.toLowerCase().endsWith(".gif")) {
            gifFrames = ImageLoader.loadGifFrames(imageFile);
            gifFrameDelay = Math.max(20, ImageLoader.getGifFrameDelay(imageFile) / 2); // played at double speed
        }
    }

    // images with their own alpha channel keep it, others use the magenta transparent color
    private static BufferedImage loadImage(String imageFile) {
        String name = imageFile.toLowerCase();
        return name.endsWith(".png") || name.endsWith(".gif")
                ? ImageLoader.loadWithAlpha(imageFile)
                : ImageLoader.load(imageFile);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (player.touching(this)) {

            boolean addedToHotbar = gifFrames != null
                    ? map.getHotbarUI().addAnimatedItem(gifFrames, gifFrameDelay, itemUse)
                    : map.getHotbarUI().addItem(itemImage, itemUse);

            if (addedToHotbar) {
                if (pickupSounds != null) {
                    SoundPlayer.playRandom(pickupSounds);
                }
                player.collectItem();
                markCollected();
                setMapEntityStatus(MapEntityStatus.REMOVED);
            }
            
        }
    }

    // sounds played when the item is picked up
    public void setPickupSounds(String... pickupSounds) {
        this.pickupSounds = pickupSounds;
    }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        useCollectedFlag(); // keeps the item from reappearing after it's picked up and the player changes rooms
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {

        BufferedImage image = spriteSheet.getSubImage(0,0);
        // large images are shrunk to 32px in the world, small ones are doubled
        float scale = image.getWidth() > 32 ? 32f / image.getWidth() : 2;
        Frame frame = new FrameBuilder(image).withScale(scale).build();

        return new GameObject(x, y, frame);
    }
}
