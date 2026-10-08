package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.Player;
import Level.PlayerState;
import Level.TileType;
import Utils.Direction;
import Utils.Point;


public class MirrorCutsceneObject extends EnchancedMapTile {
    public MirrorCutsceneObject(Point location) {
        super(location.x, location.y, null, TileType.NOT_PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        Frame frame = new FrameBuilder(spriteSheet.getSubImage(4, 2))
                .withScale(3)
                .build();
        return new GameObject(x, y, frame);


}
