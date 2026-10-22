package GameObject;

import Engine.ItemUse;
import EnhancedMapTiles.CollectibleObject;
import Utils.Point;

// collectible bow, dropped by the first ghost the player banishes
// reusable ranged weapon: each use fires an arrow for 1 damage (see ThrownItem.arrow)
public class Bow extends CollectibleObject {
    public Bow(Point location) {
        super(location, "Weapons/bow.png", 32, ItemUse.RANGED);
        setPickupSounds(KnifeSlash.PICKUP_SOUNDS);
    }
}
