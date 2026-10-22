package EnhancedMapTiles;

import Builders.FrameBuilder;
import Engine.ImageLoader;

import GameObject.Frame;
import GameObject.GameObject;
import GameObject.SpriteSheet;
import Level.EnhancedMapTile;
import Level.TileType;
import Utils.Point;

import Engine.SoundPlayer;

// A cellar door set into the ground -- give it a DoorScript as its interact script to make it lead somewhere
public class CellarDoor extends EnhancedMapTile {
    public CellarDoor(Point location) {
        super(location.x, location.y, new SpriteSheet(ImageLoader.load("CellarDoor.png"), 16, 16), TileType.NOT_PASSABLE);
    }

    @Override
    protected GameObject loadBottomLayer(SpriteSheet spriteSheet) {
        // interacting the door makes noise only in the basement
        SoundPlayer.play("openingDoor.wav");
        Frame frame = new FrameBuilder(spriteSheet.getSubImage(0, 0))
                .withScale(3)
                .build();
        return new GameObject(x, y, frame);
    }
}
