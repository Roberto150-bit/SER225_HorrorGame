package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.TileType;
import Utils.Point;

public class CollectibleObject extends EnhancedMapTile {
    
    public CollectibleObject(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("Rock.png"), 16, 16), TileType.PASSABLE);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        if (player.touching(this)) {
            System.out.println("Collectible touched");
        }
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {

        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0,0)).withScale(2).build();

        return new GameObject(x, y, frame);
    }
}
