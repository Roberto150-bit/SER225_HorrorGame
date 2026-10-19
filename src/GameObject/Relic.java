package GameObject;

import Engine.ItemUse;
import EnhancedMapTiles.CollectibleObject;
import Utils.Point;

// rare collectible relics, thrown for 3 damage and used up after one throw
// source images are 256x256
public class Relic extends CollectibleObject {

    public Relic(Point location, String imageFile) {
        super(location, imageFile, 256, ItemUse.SINGLE_USE);
    }

    public static class BloodyEye extends Relic {
        public BloodyEye(Point location) {
            super(location, "BloodyEye.gif");
        }
    }

    public static class Ear extends Relic {
        public Ear(Point location) {
            super(location, "ear.png");
        }
    }

    public static class Crucifix extends Relic {
        public Crucifix(Point location) {
            super(location, "crucifix.gif");
        }
    }
}
