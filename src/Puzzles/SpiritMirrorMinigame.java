package Puzzles;
//hint done
import Engine.GraphicsHandler;
import Engine.ImageLoader;
import Engine.MirrorCutscene;
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
    private boolean hintVisible;

    // Shared mirror area for every difficulty.
    private static final int MIRROR_X = 304;
    private static final int MIRROR_Y = 157;
    private static final int MIRROR_SIZE = 192;

    // Hint plaque (bottom-left "?"). Same values as the other puzzles;
    // recalibrate if this panel's artwork sits in a different spot.
    private static final int HINT_X = 110;
    private static final int HINT_Y = 365;
    private static final int HINT_W = 121;
    private static final int HINT_H = 80;

    private static final Color GOLD =
        new Color(245, 210, 155);

    public SpiritMirrorMinigame(PuzzleConfig config) {

        // the reflection flashes into the demon eye the first time Dean looks into the mirror
        MirrorCutscene.startOnce();

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

    // Number of shards that are not yet upright.
    private int countTurnedShards() {

        int count = 0;

        for (int rotation : currentRotations) {
            if (rotation != 0) {
                count++;
            }
        }

        return count;
    }

    // Index of the first shard that is not upright, or -1.
    private int getFirstTurnedShard() {

        for (int i = 0; i < fragmentCount; i++) {
            if (currentRotations[i] != 0) {
                return i;
            }
        }

        return -1;
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

            // Hint: outline the first shard that is still turned.
            // Drawn after all shards so no other piece covers it.
            int hintIndex = hintVisible && !solved
                ? getFirstTurnedShard()
                : -1;

            if (hintIndex >= 0) {

                int hintRow = hintIndex / gridSize;
                int hintCol = hintIndex % gridSize;

                java.awt.Polygon hintShard =
                    createShardPolygon(hintRow, hintCol, tileSize);

                double hintCenterX =
                    MIRROR_X + hintCol * tileSize + tileSize / 2.0;

                double hintCenterY =
                    MIRROR_Y + hintRow * tileSize + tileSize / 2.0;

                Graphics2D outline = (Graphics2D) board.create();

                try {
                    outline.rotate(
                        Math.toRadians(currentRotations[hintIndex] * 90),
                        hintCenterX, hintCenterY
                    );

                    outline.setColor(GOLD);

                    outline.setStroke(
                        new java.awt.BasicStroke(3f)
                    );

                    outline.drawPolygon(hintShard);

                } finally {
                    outline.dispose();
                }
            }
        } finally {
            board.dispose();
        }

        // Bottom plaque.
        String label;

        if (solved) {
            label = "REFLECTION RESTORED";
        } else if (hintVisible) {
            int turned = countTurnedShards();
            label = turned + (turned == 1
                ? " SHARD STILL TURNED"
                : " SHARDS STILL TURNED");
        } else {
            label = "ROTATE THE SHARDS";
        }

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

    // The caller can use this to let clicks on the plaque
    // through its insidePuzzle filter.
    public boolean isHintClick(int mouseX, int mouseY) {
        return mouseX >= HINT_X &&
               mouseX <= HINT_X + HINT_W &&
               mouseY >= HINT_Y &&
               mouseY <= HINT_Y + HINT_H;
    }


    @Override
    public void onClick(int mouseX, int mouseY) {

        if (solved) return;

        // Check the hint plaque before the mirror area.
        if (isHintClick(mouseX, mouseY)) {
            hintVisible = !hintVisible;
            return;
        }

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
        hintVisible = false;

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