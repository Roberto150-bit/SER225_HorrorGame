package Tilesets;

import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
//Its not possible to have a background with less than 9x9 tiles.

//Total number of tiles in the tileset: 14 x 7 = 98

public class BasementTileset extends Tileset {

    private static final int COLUMNS = 14;
    private static final int ROWS = 9;
    private static final int TOTAL_TILES = COLUMNS * ROWS;

    public BasementTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("BasementTilesetVersion4_grid.png"), 32, 32, 2);
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

        for (int i = 0; i < TOTAL_TILES; i++){
            if (i < COLUMNS || i >= TOTAL_TILES - COLUMNS || i % COLUMNS == 0 || i % COLUMNS == COLUMNS - 1) {
                solidTiles.add(i);
            }
            if (i < COLUMNS) {
                topTiles.add(i);
            }
            if (i >= TOTAL_TILES - COLUMNS) {
                bottomTiles.add(i);
            }
            if (i % COLUMNS == 0) {
                leftTiles.add(i);
            }
            if (i % COLUMNS == COLUMNS - 1) {
                rightTiles.add(i);
            }
            if (i < COLUMNS && i % COLUMNS != 0 && i % COLUMNS != COLUMNS - 1) {
                cornerTiles.add(i);
            }
            //Specific tiles here:
            if (i == 43){
                solidTiles.add(i);
            }

        }
// need to add        .withBounds(1, 2, 14, 14)

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;

                FrameBuilder builder = new FrameBuilder(getSubImage(row, col))
                    .withScale(tileScale);

                if (cornerTiles.contains(index)) {
                    builder = builder.withBounds(2, 30, 32, 1);
                } else if (topTiles.contains(index)) { // If statement determines bounds of the non-passable edges of the tileset.
                    builder = builder.withBounds(2, 0, 16, 1);
                } else if (bottomTiles.contains(index)) {
                    builder = builder.withBounds(2, 10, 16, 4);
                } else if (leftTiles.contains(index)) {
                    builder = builder.withBounds(10, 4, 10, 32);
                } else if (rightTiles.contains(index)) {
                    builder = builder.withBounds(15, 4, 10, 32);
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
