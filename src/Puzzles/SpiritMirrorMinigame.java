
package Puzzles;

import Engine.GraphicsHandler;
import Engine.ImageLoader;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class SpiritMirrorMinigame implements PuzzleMinigame {

    private final int gridSize;
    private final int fragmentCount;

    private final int[] currentRotations;
    private final int[] startingRotations;

    private final BufferedImage reflection;

    private boolean solved;

    // Shared mirror area for every difficulty.
    private static final int MIRROR_X = 304;
    private static final int MIRROR_Y = 157;
    private static final int MIRROR_SIZE = 192;

    private static final Color GOLD =
        new Color(245, 210, 155);

    public SpiritMirrorMinigame(PuzzleConfig config) {
        
        reflection = ImageLoader.load("../Resources/Puzzles/SpiritMirror/Fragments/spirit_reflection.png");

        switch (config.getDifficulty()) {
            case EASY:
                gridSize = 2;
                break;

            case MEDIUM:
                gridSize = 3;
                break;

            case HARD:
                gridSize = 4;
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown difficulty"
                );
        }

        fragmentCount = gridSize * gridSize;

        currentRotations = new int[fragmentCount];
        startingRotations = new int[fragmentCount];

        // Keep the reflection initialization already used
        // by your project here, if it exists.

        generateStartingRotations();
        reset();
    }

    private void generateStartingRotations() {

        Random random = new Random();

        boolean anyRotated = false;

        for (int i = 0; i < fragmentCount; i++) {

            startingRotations[i] = random.nextInt(4);

            if (startingRotations[i] != 0) {
                anyRotated = true;
            }
        }

        // Ensure the mirror does not start completed.
        if (!anyRotated) {
            startingRotations[random.nextInt(fragmentCount)] = 1;
        }
    }

    // Rotate the selected fragment by 90 degrees.
    public void rotateFragment(int index) {

        if (solved || index < 0 ||
            index >= fragmentCount) {
            return;
        }

        currentRotations[index] =
            (currentRotations[index] + 1) % 4;

        checkSolution();
    }

    // All image pieces must return to upright.
    private void checkSolution() {

        for (int rotation : currentRotations) {
            if (rotation != 0) {
                return;
            }
        }

        solved = true;
    }

    @Override
    public void update() {
        // Mouse controls the fragments.
    }


    @Override
    public void draw(GraphicsHandler graphicsHandler) {

        Graphics2D g = graphicsHandler.getGraphics();

        if (reflection == null) return;

        int tileSize = MIRROR_SIZE / gridSize;

        // The entire puzzle must fit inside the oval mirror.
        java.awt.geom.Ellipse2D mirrorArea =
            new java.awt.geom.Ellipse2D.Double(
                MIRROR_X, MIRROR_Y,
                MIRROR_SIZE, MIRROR_SIZE
            );

        Graphics2D board = (Graphics2D) g.create();

        try {
            board.clip(mirrorArea);

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {

                    int index = row * gridSize + col;

                    java.awt.Polygon shard =
                        createShardPolygon(row, col, tileSize);

                    double centerX =
                        MIRROR_X + col * tileSize + tileSize / 2.0;

                    double centerY =
                        MIRROR_Y + row * tileSize + tileSize / 2.0;

                    Graphics2D piece = (Graphics2D) board.create();

                    try {
                        // Rotate the shard and its image together.
                        piece.rotate(
                            Math.toRadians(currentRotations[index] * 90),
                            centerX, centerY
                        );

                        piece.clip(shard);

                        // Draw the complete reflection through
                        // this fragment's irregular shape.
                        piece.drawImage(
                            reflection,
                            MIRROR_X, MIRROR_Y,
                            MIRROR_SIZE, MIRROR_SIZE,
                            null
                        );

                        piece.setColor(
                            new Color(190, 220, 235)
                        );

                        piece.setStroke(
                            new java.awt.BasicStroke(1.2f)
                        );

                        piece.drawPolygon(shard);

                    } finally {
                        piece.dispose();
                    }
                }
            }
        } finally {
            board.dispose();
        }

        // Bottom plaque.
        String label = solved
            ? "REFLECTION RESTORED"
            : "ROTATE THE SHARDS";
        Font font = new Font("Serif", Font.BOLD, 12);

        g.setFont(font);

        int textWidth =
            g.getFontMetrics().stringWidth(label);

        graphicsHandler.drawString(
            label,
            400 - textWidth / 2,
            390,
            font,
            GOLD
        );
    }


    private java.awt.Polygon createShardPolygon(
            int row, int col, int size) {

        int x = MIRROR_X + col * size;
        int y = MIRROR_Y + row * size;

        // Shared irregular edges.
        int variation = Math.max(3, size / 7);

        int top = row == 0 ? 0 :
            (((row * 13 + col * 7) % 3) - 1) * variation;

        int bottom = row == gridSize - 1 ? 0 :
            ((((row + 1) * 13 + col * 7) % 3) - 1)
            * variation;

        int left = col == 0 ? 0 :
            (((row * 7 + col * 11) % 3) - 1) * variation;

        int right = col == gridSize - 1 ? 0 :
            (((row * 7 + (col + 1) * 11) % 3) - 1)
            * variation;

        int[] xs = {
            x,
            x + size / 2,
            x + size,
            x + size + right,
            x + size,
            x + size / 2,
            x,
            x + left
        };

        int[] ys = {
            y,
            y + top,
            y,
            y + size / 2,
            y + size,
            y + size + bottom,
            y + size,
            y + size / 2
        };

        return new java.awt.Polygon(xs, ys, 8);
    }


    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) return;

        java.awt.geom.Ellipse2D mirrorArea =
            new java.awt.geom.Ellipse2D.Double(
                MIRROR_X, MIRROR_Y,
                MIRROR_SIZE, MIRROR_SIZE
            );

        if (!mirrorArea.contains(mouseX, mouseY)) {
            return;
        }

        int tileSize = MIRROR_SIZE / gridSize;

        // Check from last-drawn shard to first so
        // overlapping rotated fragments click correctly.
        for (int index = fragmentCount - 1; index >= 0; index--) {

            int row = index / gridSize;
            int col = index % gridSize;

            java.awt.Polygon shard =
                createShardPolygon(row, col, tileSize);

            double centerX =
                MIRROR_X + col * tileSize + tileSize / 2.0;

            double centerY =
                MIRROR_Y + row * tileSize + tileSize / 2.0;

            java.awt.geom.AffineTransform rotation =
                java.awt.geom.AffineTransform.getRotateInstance(
                    Math.toRadians(currentRotations[index] * 90),
                    centerX, centerY
                );

            if (rotation.createTransformedShape(shard)
                    .contains(mouseX, mouseY)) {

                rotateFragment(index);
                return;
            }
        }
    }


    @Override
    public boolean isSolved() {
        return solved;
    }

    @Override
    public void reset() {
        solved = false;

        for (int i = 0; i < fragmentCount; i++) {
            currentRotations[i] = startingRotations[i];
        }
    }

    public int getFragmentCount() {
        return fragmentCount;
    }

    public int getCurrentRotation(int index) {
        return currentRotations[index];
    }

    // The restored reflection uses zero rotation.
    public int getTargetRotation(int index) {
        return 0;
    }
}
