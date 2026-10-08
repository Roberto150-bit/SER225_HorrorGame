package Puzzles;

import Engine.ImageLoader;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class PuzzleAssets {

    // Stores loaded images so they are not repeatedly loaded.
    private static final Map<String, BufferedImage> images =
            new HashMap<>();

    // Loads an image once and reuses it afterward.
    public static BufferedImage get(String fileName) {

        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Invalid image name");
        }

        return images.computeIfAbsent(
                fileName,
                name -> ImageLoader.load("Puzzles/" + name)
        );
    }
}
