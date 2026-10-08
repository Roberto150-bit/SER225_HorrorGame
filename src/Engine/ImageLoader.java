package Engine;

import Utils.ImageUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// contains a bunch of helpful methods for loading images file into the game
public class ImageLoader {

    // loads an image and sets its transparent color to the one defined in the Config class
    public static BufferedImage load(String imageFileName) {
        return ImageLoader.load(imageFileName, Config.TRANSPARENT_COLOR);
    }

    // loads an image as-is, keeping its own alpha channel (no transparent color is applied)
    public static BufferedImage loadWithAlpha(String imageFileName) {
        try {
            return ImageIO.read(new File(Config.RESOURCES_PATH + imageFileName));
        } catch (IOException e) {
            System.out.println("Unable to find file " + Config.RESOURCES_PATH + imageFileName);
            throw new RuntimeException(e);
        }
    }

    // loads an image and allows the transparent color to be specified
    public static BufferedImage load(String imageFileName, Color transparentColor) {
        try {
            BufferedImage initialImage = ImageIO.read(new File(Config.RESOURCES_PATH + imageFileName));
            return ImageUtils.transformColorToTransparency(initialImage, transparentColor);
        } catch (IOException e) {
            System.out.println("Unable to find file " + Config.RESOURCES_PATH + imageFileName);
            throw new RuntimeException(e);
        }
    }

    // loads a piece of an image from an image file and sets its transparent color to the one defined in the Config class
    public static BufferedImage loadSubImage(String imageFileName, int x, int y, int width, int height) {
        return ImageLoader.loadSubImage(imageFileName, Config.TRANSPARENT_COLOR, x, y, width, height);
    }

    // loads a piece of an image from an image file and allows the transparent color to be specified
    public static BufferedImage loadSubImage(String imageFileName, Color transparentColor, int x, int y, int width, int height) {
        try {
            BufferedImage initialImage = ImageIO.read(new File(Config.RESOURCES_PATH + imageFileName));
            BufferedImage transparentImage = ImageUtils.transformColorToTransparency(initialImage, transparentColor);
            return transparentImage.getSubimage(x, y, width, height);
        } catch (IOException e) {
            System.out.println("Unable to find file " + Config.RESOURCES_PATH + imageFileName);
            throw new RuntimeException(e);
        }
    }

    // loads every frame of a gif, each composited onto a full-size canvas so partial frames draw correctly
    public static BufferedImage[] loadGifFrames(String imageFileName) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new File(Config.RESOURCES_PATH + imageFileName))) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(stream);
            int count = reader.getNumImages(true);
            BufferedImage[] frames = new BufferedImage[count];
            BufferedImage canvas = null;
            for (int i = 0; i < count; i++) {
                BufferedImage frame = reader.read(i);
                if (canvas == null) {
                    canvas = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                }
                // gif frames are cleared to the background between frames
                java.awt.Graphics2D g = canvas.createGraphics();
                g.setComposite(java.awt.AlphaComposite.Clear);
                g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                g.setComposite(java.awt.AlphaComposite.SrcOver);
                g.drawImage(frame, 0, 0, null);
                g.dispose();
                BufferedImage copy = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
                copy.getGraphics().drawImage(canvas, 0, 0, null);
                frames[i] = copy;
            }
            reader.dispose();
            return frames;
        } catch (IOException e) {
            System.out.println("Unable to find file " + Config.RESOURCES_PATH + imageFileName);
            throw new RuntimeException(e);
        }
    }

    // returns the delay of the first frame of a gif in milliseconds (defaults to 100 if unspecified)
    public static int getGifFrameDelay(String imageFileName) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new File(Config.RESOURCES_PATH + imageFileName))) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(stream);
            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(0).getAsTree("javax_imageio_gif_image_1.0");
            reader.dispose();
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = gce == null ? 0 : Integer.parseInt(gce.getAttribute("delayTime")) * 10;
            return delay > 0 ? delay : 100;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
