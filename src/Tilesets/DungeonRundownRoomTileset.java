package Tilesets;
 
import Builders.FrameBuilder;
import Builders.MapTileBuilder;
import Engine.ImageLoader;
import Level.TileType;
import Level.Tileset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
//This map is done differently than the other maps because it has a lot of partial bounds and solid tiles that are not on the edges of the map. 
public class DungeonRundownRoomTileset extends Tileset {
    //must be at least 9x9 tiles, or else the background will not render correctly. My base map is 7 rows by 8 columns.
    //the first two rows and first column are solid black tiles (seperate from the rest of the tileset) that are there to fix the 9x9 tile requirement. The rest of the tileset is 7 rows by 8 columns, for a total of 56 tiles.
    private static final int COLUMNS = 7;
    private static final int ROWS = 8;
 
    private static final int TOTAL_TILES = COLUMNS * ROWS;
 
    public DungeonRundownRoomTileset() { //32 by 32 pixels, scaled by 3 for a total of 96 by 96 pixels
        super(ImageLoader.load("Room3Tileset.png"), 32, 32, 2);
    }
 
    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> tiles = new ArrayList<>();


        ArrayList<Integer> solidTiles = new ArrayList<>();
 
        // Tiles that only block part of the square: index -> {x, y, width, height} in tile pixels.
        Map<Integer, int[]> partialBounds = new HashMap<>();
 
        // Solid black cave rock
        solidTiles.addAll(Arrays.asList(
                0, 1, 2, 6,     // top row, left and right of the floor
                7,              // upper left rock
                27,             // notch in the right wall
                28, 49,         // lower left rock
                48,             // lower right rock
                54, 55          // bottom right rock
                
        ));
 
        // Top wall: floor starts about 22px down in the upper right section
        partialBounds.put(3, new int[]{0, 0, 32, 22});
        partialBounds.put(4, new int[]{0, 0, 32, 22});
        partialBounds.put(5, new int[]{0, 0, 32, 22});
        partialBounds.put(8, new int[]{0, 0, 32, 22});
        // Diagonal rock edge at the upper left of the floor
        partialBounds.put(9, new int[]{0, 0, 20, 28});
 
        // Left wall: floor starts about 20px in from the left edge
        partialBounds.put(14, new int[]{0, 0, 20, 32});
        partialBounds.put(21, new int[]{0, 0, 20, 32});
        partialBounds.put(35, new int[]{0, 0, 20, 32});
        partialBounds.put(42, new int[]{0, 0, 20, 32});
 
        // Right wall: floor ends about 12px into the last column
        partialBounds.put(13, new int[]{12, 0, 20, 32});
        partialBounds.put(20, new int[]{12, 0, 20, 32});
        partialBounds.put(34, new int[]{12, 0, 20, 32});
        partialBounds.put(41, new int[]{12, 0, 20, 32});
 
        // Vertical chasm in the middle (tiles 18, 25, 32)
        partialBounds.put(18, new int[]{10, 10, 12, 22});
        partialBounds.put(25, new int[]{8, 0, 24, 32});
        partialBounds.put(32, new int[]{10, 0, 14, 20});
 
        // Horizontal crack branching right off the chasm
        partialBounds.put(26, new int[]{0, 6, 32, 18});
 
        // Crack on the left side (tiles 29, 30)
        partialBounds.put(29, new int[]{0, 7, 32, 17});
        partialBounds.put(30, new int[]{0, 6, 21, 19});
 
        // Rock jutting in at the lower right
        partialBounds.put(47, new int[]{12, 13, 16, 19});
 
        // Bottom wall: floor ends about 11px into the last row.
        // The doorway in column 2 (tile 51) and the corridor beside it stay open.
        partialBounds.put(50, new int[]{0, 11, 22, 21});
        partialBounds.put(51, new int[]{0, 11, 22, 21});
        partialBounds.put(52, new int[]{11, 11, 21, 21});
        partialBounds.put(53, new int[]{0, 11, 32, 21});
        

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;
 
                FrameBuilder builder = new FrameBuilder(getSubImage(row, col))
                    .withScale(tileScale);
 
                int[] b = partialBounds.get(index);
                if (b != null) {
                    builder = builder.withBounds(b[0], b[1], b[2], b[3]);
                }
                MapTileBuilder tile = new MapTileBuilder(builder.build());
 
                if (b != null || solidTiles.contains(index)) {
                    tile.withTileType(TileType.NOT_PASSABLE);
                }
 
                tiles.add(tile);
            }
        }
        return tiles;
    }
}
