package EnhancedMapTiles;

import java.awt.image.BufferedImage;
import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.MapEntityStatus;
import Level.Player;
import Level.TileType;
import Utils.Point;


public class CollectibleObject extends EnhancedMapTile {
    
    private BufferedImage itemImage = ImageLoader.load("Rock.png");

    public CollectibleObject(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("Rock.png"), 14, 14), TileType.PASSABLE);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (player.touching(this)) {

            boolean addedToHotbar = map.getHotbarUI().addItem(itemImage);

            if (addedToHotbar) {
                player.collectItem();
                setMapEntityStatus(MapEntityStatus.REMOVED);
            }
            
        }
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {

        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0,0)).withScale(2).build();

        return new GameObject(x, y, frame);
    }
}
