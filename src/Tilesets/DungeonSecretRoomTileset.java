package Tilesets;
//modified not tested
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;

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
        for (int rows = 0; rows < ROWS; rows++) {
            for (int cols = 0; cols < COLUMNS; cols++) {

                int indexs = rows * COLUMNS + cols;

                if (indexs < COLUMNS) {
                    topTiles.add(indexs);
                }

                if (indexs >= (ROWS - 1) * COLUMNS && indexs < ROWS * COLUMNS) {
                    bottomTiles.add(indexs);
                }

                if (indexs % COLUMNS == 0) {
                    leftTiles.add(indexs);
                }

                if (indexs % COLUMNS == COLUMNS - 1) {
                    rightTiles.add(indexs);
                }
            }
        }

        //specific tiles here:
        solidTiles.addAll(Arrays.asList(10,11,12,13,14,15,16));//the chairs
        solidTiles.addAll(Arrays.asList(73, 79, 83, 87));//the fences
        /* 
        solidTiles.addAll(Arrays.asList(
                11, 12, 13, 14, 15,
                20, 21, 22, 23, 24,
                29, 30, 31, 32, 33
        ));

        solidTiles.addAll(Arrays.asList(
                38, 39, 40, 41, 42,
                47, 48, 49, 50, 51
        ));

        solidTiles.addAll(Arrays.asList(
                29, 31,
                37, 43,
                55, 61,
                65, 67
        ));
        */

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;

                FrameBuilder builder = new FrameBuilder(getSubImage(row, col))
                    .withScale(tileScale);

                if (topTiles.contains(index)) {
                    builder = builder.withBounds(0, 0, 32, 24);
                } else if (bottomTiles.contains(index)) {
                    builder = builder.withBounds(0, 12, 32, 20);
                } else if (leftTiles.contains(index)) {
                    builder = builder.withBounds(0, 0, 20, 32);
                } else if (rightTiles.contains(index)) {
                    builder = builder.withBounds(12, 0, 20, 32);
                }
                MapTileBuilder tile = new MapTileBuilder(builder.build());

                if (solidTiles.contains(index)
                        || topTiles.contains(index)
                        || bottomTiles.contains(index)
                        || leftTiles.contains(index)
                        || rightTiles.contains(index)) {
                    tile.withTileType(TileType.NOT_PASSABLE);
                }

                tiles.add(tile);            }
        }
        return tiles;
    }
}

