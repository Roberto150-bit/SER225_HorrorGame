package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;
import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.TileType;
import Utils.Point;


// This class is for decorations that display a UI popup 
// when the player interacts with them.
public class NoteObject extends EnhancedMapTile{
    public NoteObject(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("Note.png"), 16, 16), TileType.PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0, 0))
                .withScale(3)
                .build();
        return new GameObject(x, y, frame);
    }
}



