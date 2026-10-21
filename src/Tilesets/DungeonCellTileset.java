package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.ImageEffect;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;

public class DungeonCellTileset extends Tileset {
    //must be at least 9x9 tiles, or else the background will not render correctly. My base map is 7 rows by 8 columns.
    //the first two rows and first column are solid black tiles (seperate from the rest of the tileset) that are there to fix the 9x9 tile requirement. The rest of the tileset is 7 rows by 8 columns, for a total of 56 tiles.
    private static final int COLUMNS = 9;
    private static final int ROWS = 9;

    private static final int TilesetCols = 8;
    private static final int TilesetRows = 7;

    private static final int TOTAL_TILES = COLUMNS * ROWS;

    public DungeonCellTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("Room1TilesetVersion3WithBlackTiles.png"), 32, 32, 2);
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

        for (int i = 0; i < TOTAL_TILES; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;

            if (row < 2 || col == 0) {
                solidTiles.add(i);                       // black padding (first two rows, first column)
            } else {
                boolean topEdge    = (row == 2);
                boolean bottomEdge = (row == ROWS - 1);
                boolean leftEdge   = (col == 1);
                boolean rightEdge  = (col == COLUMNS - 1);

                if ((topEdge || bottomEdge) && (leftEdge || rightEdge)) {
                    solidTiles.add(i);                   // 4 room corners (19, 26, 73, 80): fully solid
                } else if (topEdge) {
                    solidTiles.add(i);
                    topTiles.add(i);                     // 20-25
                } else if (bottomEdge) {
                    solidTiles.add(i);
                    bottomTiles.add(i);                  // 74-79
                } else if (leftEdge) {
                    solidTiles.add(i);
                    leftTiles.add(i);                    // 28, 37, 46, 55, 64
                } else if (rightEdge) {
                    solidTiles.add(i);
                    rightTiles.add(i);                   // 35, 44, 53, 62, 71
                }
            }
        }
        //specific tiles here:
        solidTiles.addAll(Arrays.asList(34, 43, 66,75));    // 34, 43 = bed, 66 = toilet
        
// need to add        .withBounds(1, 2, 14, 14)

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

                if (solidTiles.contains(index)) {
                    tile.withTileType(TileType.NOT_PASSABLE);
                }

                tiles.add(tile);
            }
        }
        return tiles;
    }
}

