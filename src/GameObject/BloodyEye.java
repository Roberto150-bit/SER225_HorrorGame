package GameObject;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import Level.MapEntityStatus;
import Level.NPC;
import Level.Player;
import Utils.Point;
import java.awt.image.BufferedImage;

// base class for game objects the player can pick up by walking over them -- they go into the hotbar
public class BloodyEye extends NPC {

    private BufferedImage itemImage;
    private BufferedImage[] gifFrames; // only set for animated (gif) items
    private int gifFrameDelay;

    // imageFile is a square image of imageSize pixels
    public BloodyEye(Point location, String imageFile, int imageSize) {
        super(-1, location.x, location.y, buildFrame(loadImage(imageFile)));
        this.itemImage = loadImage(imageFile);
        this.isUncollidable = true; // player walks over it to pick it up
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

    // large images are shrunk to 32px in the world
    private static Frame buildFrame(BufferedImage image) {
        float scale = image.getWidth() > 32 ? 32f / image.getWidth() : 2;
        return new FrameBuilder(image).withScale(scale).build();
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

    // collectible bloody eye, can be placed in any map
    // source image is 256x256, scaled down to 32x32 in the world
    static class BloodyEyeItem extends BloodyEye {
        public BloodyEyeItem(Point location) {
            super(location, "BloodyEye.gif", 256);
        }
    }

    // collectible ear, can be placed in any map
    // source image is 256x256, scaled down to 32x32 in the world
    static class Ear extends BloodyEye {
        public Ear(Point location) {
            super(location, "ear.png", 256);
        }
    }
}
