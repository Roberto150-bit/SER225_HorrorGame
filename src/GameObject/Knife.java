package GameObject;

import Engine.ItemUse;
import EnhancedMapTiles.CollectibleObject;
import Utils.Point;

// collectible knife, reusable weapon that does 1 damage
public class Knife extends CollectibleObject {
    public Knife(Point location) {
        super(location, "Weapons/weapon_09.png", 32, ItemUse.REUSABLE);
        setPickupSounds(KnifeSlash.PICKUP_SOUNDS);
    }
}
