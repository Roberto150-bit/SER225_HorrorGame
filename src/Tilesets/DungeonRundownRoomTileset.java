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

// The Final Seal room. This map is done differently than the other maps because it has a lot of partial bounds
// and solid tiles that are not on the edges of the map.
// The room art is 7 columns by 12 rows: the original 7x8 room (Room3Tileset.png) with 4 rows added on top.
// It comes in two versions with the same layout of tile indexes:
//   - before the Final Seal breaks (Room3TilesetTall.png) the 4 new rows are solid dark rock
//   - after it breaks (Room3TilesetExpanded.png) the top chamber reaches 2 tiles further up, to a back wall
//     where the great door to the Necromancer's room stands
public class DungeonRundownRoomTileset extends Tileset {
    private static final int COLUMNS = 7;
    private static final int ROWS = 12;
    // rows added on top of the original room
    public static final int ADDED_ROWS = 4;

    // which version is being built: the Tileset constructor calls defineTiles() before this class's constructor body runs,
    // so the choice is recorded while the image is picked (in the super(...) call) and read back in defineTiles()
    private static boolean buildingExpanded;

    // the room as it starts: the original room under 4 rows of rock
    public DungeonRundownRoomTileset() {
        this(false);
    }

    // expanded = the room after the Final Seal breaks
    public DungeonRundownRoomTileset(boolean expanded) {
        super(loadImage(expanded), 32, 32, 2);
    }

    private static java.awt.image.BufferedImage loadImage(boolean expanded) {
        buildingExpanded = expanded;
        return ImageLoader.load(expanded ? "Room3TilesetExpanded.png" : "Room3TilesetTall.png");
    }

    @Override
    public ArrayList<MapTileBuilder> defineTiles() {
        ArrayList<MapTileBuilder> tiles = new ArrayList<>();
        boolean expanded = buildingExpanded;

        // indexes below are for the original 7x8 room; they're shifted down ADDED_ROWS rows further down
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

        if (expanded) {
            // the top chamber now carries on upward, so its old top edge is open floor,
            // and the right wall's edge continues up the side (the old top-right corner rock is now wall edge)
            partialBounds.remove(3);
            partialBounds.remove(4);
            partialBounds.remove(5);
            solidTiles.remove(Integer.valueOf(6));
            partialBounds.put(6, new int[]{12, 0, 20, 32});
            // the column left of the chamber: rock with the floor's edge in its right third
            solidTiles.remove(Integer.valueOf(2));
            partialBounds.put(2, new int[]{0, 0, 20, 32});
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int index = row * COLUMNS + col;

                FrameBuilder builder = new FrameBuilder(getSubImage(row, col))
                    .withScale(tileScale);

                int[] bounds = null;
                boolean solid;
                if (row < ADDED_ROWS) {
                    // the added rows: rock, except (once expanded) the new top of the chamber
                    int[] added = expanded ? expandedTopBounds(row, col) : null;
                    if (added != null && added.length == 0) {
                        solid = false;            // open floor
                    } else {
                        bounds = added;
                        solid = true;
                    }
                } else {
                    int original = index - ADDED_ROWS * COLUMNS;
                    bounds = partialBounds.get(original);
                    solid = bounds != null || solidTiles.contains(original);
                }

                if (bounds != null) {
                    builder = builder.withBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
                }
                MapTileBuilder tile = new MapTileBuilder(builder.build());
                if (solid) {
                    tile.withTileType(TileType.NOT_PASSABLE);
                }
                tiles.add(tile);
            }
        }
        return tiles;
    }

    // in the expanded room, row 3 of the added rows is the new floor at the top of the chamber (rows 0-2 stay rock,
    // that's the back wall the great door stands in)
    // returns an empty array for open floor, partial bounds for an edge, or null for solid rock
    private static int[] expandedTopBounds(int row, int col) {
        if (row != ADDED_ROWS - 1) {
            return null;
        }
        switch (col) {
            case 2: return new int[]{0, 0, 20, 32};   // floor's left edge
            case 3: case 4: case 5: return new int[0];
            case 6: return new int[]{12, 0, 20, 32};  // floor's right edge
            default: return null;
        }
    }
}
