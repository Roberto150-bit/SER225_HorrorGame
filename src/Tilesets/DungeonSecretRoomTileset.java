package Tilesets;
//modified not tested
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DungeonSecretRoomTileset extends Tileset {
    //must be at least 9x9 tiles, or else the background will not render correctly. My base map is 7 rows by 8 columns.
    //the first two rows and first column are solid black tiles (seperate from the rest of the tileset) that are there to fix the 9x9 tile requirement. The rest of the tileset is 7 rows by 8 columns, for a total of 56 tiles.
    private static final int COLUMNS = 9;
    private static final int ROWS = 11;

    private static final int TOTAL_TILES = COLUMNS * ROWS;

    public DungeonSecretRoomTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("SecretRoomTileset.png"), 32, 32, 2);
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> tiles = new ArrayList<>();

        //This is where you create the tiles, for reference, see CommonTileset.java

        ArrayList<Integer> solidTiles = new ArrayList<>();
        ArrayList<Integer> topTiles = new ArrayList<>();
        ArrayList<Integer> bottomTiles = new ArrayList<>();
        ArrayList<Integer> leftTiles = new ArrayList<>();
        ArrayList<Integer> rightTiles = new ArrayList<>();
        ArrayList<Integer> cornerTiles = new ArrayList<>();

        // Black padding and outer boundaries
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {

                int index = row * COLUMNS + col;

                // Padding at the top and outer sides
                if (row == 0 || col == 0 || col == COLUMNS - 1) {
                    solidTiles.add(index);
                    continue;
                }

                // Top wall/shelf area
                boolean topEdge = row == 1;

                // Side walls around the room
                boolean leftEdge = col == 1;
                boolean rightEdge = col == 7;

                // Bottom wall (the doorway at col 4 is the door tile itself, so it's solid too)
                boolean bottomEdge = row == 10;

                if (topEdge) {
                    solidTiles.add(index);
                    topTiles.add(index);
                } else if (bottomEdge) {
                    solidTiles.add(index);
                } else if (leftEdge) {
                    solidTiles.add(index);
                    leftTiles.add(index);
                } else if (rightEdge) {
                    solidTiles.add(index);
                    rightTiles.add(index);
                }
            }
        }

        // Tiles that only block part of the square: index -> {x, y, width, height} in tile pixels.
        // the candle stands and the edges of the blood pool are small, so the player can walk around them
        Map<Integer, int[]> partialBounds = new HashMap<>();

        // candle stands above the pool, at (3,3) and (5,3)
        partialBounds.put(index(3, 3), new int[]{8, 6, 16, 20});
        partialBounds.put(index(5, 3), new int[]{8, 6, 16, 20});

        // candle stands beside the pool's top corners, at (2,4) and (6,4)
        partialBounds.put(index(2, 4), new int[]{12, 12, 20, 20});
        partialBounds.put(index(6, 4), new int[]{0, 12, 20, 20});

        // top edge of the pool
        partialBounds.put(index(3, 4), new int[]{0, 20, 32, 12});
        partialBounds.put(index(4, 4), new int[]{0, 20, 32, 12});
        partialBounds.put(index(5, 4), new int[]{0, 20, 32, 12});

        // left and right edges of the pool
        partialBounds.put(index(2, 5), new int[]{18, 0, 14, 32});
        partialBounds.put(index(6, 5), new int[]{0, 0, 13, 32});

        // candle stands beside the pool's bottom corners, at (2,6) and (6,6)
        partialBounds.put(index(2, 6), new int[]{12, 0, 20, 20});
        partialBounds.put(index(6, 6), new int[]{0, 0, 20, 20});

        // bottom edge of the pool
        partialBounds.put(index(3, 6), new int[]{0, 0, 32, 13});
        partialBounds.put(index(4, 6), new int[]{0, 0, 32, 13});
        partialBounds.put(index(5, 6), new int[]{0, 0, 32, 13});

        // candle stands below the pool, at (3,7) and (5,7)
        partialBounds.put(index(3, 7), new int[]{8, 4, 16, 20});
        partialBounds.put(index(5, 7), new int[]{8, 4, 16, 20});

        // middle of the pool
        solidTiles.addAll(Arrays.asList(index(3, 5), index(4, 5), index(5, 5)));

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;

                FrameBuilder builder = new FrameBuilder(getSubImage(row, col))
                    .withScale(tileScale);

                int[] b = partialBounds.get(index);
                if (b != null) {
                    builder = builder.withBounds(b[0], b[1], b[2], b[3]);
                    solidTiles.add(index);
                } else if (topTiles.contains(index)) {
                    builder = builder.withBounds(0, 0, 32, 24);
                } else if (bottomTiles.contains(index)) {
                    builder = builder.withBounds(0, 12, 32, 20);
                } else if (leftTiles.contains(index)) {
                    builder = builder.withBounds(0, 0, 20, 32);
                } else if (rightTiles.contains(index)) {
                    builder = builder.withBounds(12, 0, 20, 32);
                }
                MapTileBuilder tile = new MapTileBuilder(builder.build());

                if (solidTiles.contains(index)) {
                    tile.withTileType(TileType.NOT_PASSABLE);
                }

                tiles.add(tile);
            }
        }
        return tiles;
    }

    // tileset index of the tile at column x, row y
    private static int index(int x, int y) {
        return y * COLUMNS + x;
    }
}

