package Tilesets;
//modify
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;

public class DungeonBasicRoomTileset extends Tileset {
    //must be at least 9x9 tiles, or else the background will not render correctly. My base map is 7 rows by 8 columns.
    //the first two rows and first column are solid black tiles (seperate from the rest of the tileset) that are there to fix the 9x9 tile requirement. The rest of the tileset is 7 rows by 8 columns, for a total of 56 tiles.
    private static final int COLUMNS = 9;
    private static final int ROWS = 9;

    private static final int TOTAL_TILES = COLUMNS * ROWS;

    public DungeonBasicRoomTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("Room4Tileset.png"), 32, 32, 2);
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
 
                // Black padding above and to the left of the room
                if (row < 2 || col == 0) {
                    solidTiles.add(index);
                    continue;
                }
 
                // The room is a rectangle: top wall in row 2, left wall in column 1,
                // and the bottom/right walls in the outermost row/column.
                // Their partial bounds leave the rest of that tile walkable.
                boolean topEdge = row == 2;
                boolean bottomEdge = row == ROWS - 1;
                boolean leftEdge = col == 1;
                boolean rightEdge = col == COLUMNS - 1;
 
                if ((topEdge || bottomEdge)
                        && (leftEdge || rightEdge)) {
                    solidTiles.add(index);
                } else if (topEdge) {
                    solidTiles.add(index);
                    topTiles.add(index);
                } else if (bottomEdge) {
                    solidTiles.add(index);
                    bottomTiles.add(index);
                } else if (leftEdge) {
                    solidTiles.add(index);
                    leftTiles.add(index);
                } else if (rightEdge) {
                    solidTiles.add(index);
                    rightTiles.add(index);
                }
            }
        }
        //specific tiles here:
        

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


