package Tilesets;
//untested but should work
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;

public class DungeonLibraryTileset extends Tileset {
    //must be at least 9x9 tiles, or else the background will not render correctly. 
    //the first two rows and first column are solid black tiles (seperate from the rest of the tileset) that are there to fix the 9x9 tile requirement. The rest of the tileset is 7 rows by 8 columns, for a total of 56 tiles.
    private static final int COLUMNS = 9;
    private static final int ROWS = 11;

    private static final int TOTAL_TILES = COLUMNS * ROWS;

    public DungeonLibraryTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("LibraryRoomTileset.png"), 32, 32, 2);
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

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
 
                int index = row * COLUMNS + col;
 
                // Black padding above and to the left of the room
                if (row == 0 || col == 0) {
                    solidTiles.add(index);
                    continue;
                }
 
                // The top and left walls are in rows/columns 1, but the bottom and
                // right walls are in the outermost row/column (ROWS - 1, COLUMNS - 1).
                // Their partial bounds leave the rest of that tile walkable.
                boolean topEdge = row == 1;
                boolean bottomEdge = row == ROWS - 1;
                boolean leftEdge = col == 1;
                boolean rightEdge = col == COLUMNS - 1;
 
                // Room corners
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
        //specific tiles here 
 
        // Top bookshelves 
        solidTiles.addAll(Arrays.asList(13, 22, 31));
        solidTiles.addAll(Arrays.asList(15, 24, 33));
 
        // Table
        solidTiles.add(29);
 
        // Right bookshelf 
        solidTiles.addAll(Arrays.asList(70, 79, 88));
 
        // Bottom bookshelves 
        solidTiles.addAll(Arrays.asList(85, 86, 87,
            94,95,96

        ));
        

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

