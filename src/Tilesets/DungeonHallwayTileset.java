package Tilesets;
//tested and implemented
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;

public class DungeonHallwayTileset extends Tileset {
    //must be at least 9x9 tiles
    //the first two rows and first column are solid black tiles that are there to fix the 9x9 tile requirement. 
    private static final int COLUMNS = 9;
    private static final int ROWS = 9;

    private static final int TOTAL_TILES = COLUMNS * ROWS;

    

    public DungeonHallwayTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels 
        super(ImageLoader.load("DungeonHallwayTilesetV2.png"), 32, 32, 2);
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
        ArrayList<Integer> walkableTiles = new ArrayList<>();

        walkableTiles.addAll(Arrays.asList(19, 28, 37, 38, 39, 40, 41, 42, 43, 49, 58, 67)); //10, 44, 76 are the doors
        leftTiles.addAll(Arrays.asList(18, 27, 36, 57, 66));
        rightTiles.addAll(Arrays.asList(20, 59, 68));
        topTiles.addAll(Arrays.asList(30, 31, 32, 33, 34));
        bottomTiles.addAll(Arrays.asList(46, 47, 51, 52));
        cornerTiles.addAll(Arrays.asList(29, 48, 50));

        for (int i = 0; i < TOTAL_TILES; i++) {
            if (!walkableTiles.contains(i)) {
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
                    // concave corners: wall is a single block in one quadrant
                    if (index == 29) {                       // wall top-right
                        builder = builder.withBounds(11, 0, 21, 23);
                    } else if (index == 48) {                // wall bottom-left
                        builder = builder.withBounds(0, 11, 21, 21);
                    } else if (index == 50) {                // wall bottom-right
                        builder = builder.withBounds(11, 11, 21, 21);
                    }
                } else if (topTiles.contains(index)) {       // wall above, floor below
                    builder = builder.withBounds(0, 0, 32, 23);
                } else if (bottomTiles.contains(index)) {    // wall below, floor above
                    builder = builder.withBounds(0, 11, 32, 21);
                } else if (leftTiles.contains(index)) {      // wall on the left, floor right
                    builder = builder.withBounds(0, 0, 21, 32);
                } else if (rightTiles.contains(index)) {     // wall on the right, floor left
                    builder = builder.withBounds(11, 0, 21, 32);
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

