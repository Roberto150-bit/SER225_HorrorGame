package Scripts;

import java.util.ArrayList;
import java.util.function.Supplier;

import Level.Map;
import Level.Script;
import ScriptActions.*;
import Utils.Direction;

// Trigger script for doors/entrances -- when the player walks into the trigger, they are moved to another map
// example usage in a Map's loadTriggers():
//     triggers.add(new Trigger(x, y, width, height, new DoorScript(() -> new HallwayMap(), 5, 3, Direction.DOWN)));
// the spawn tile in the destination map should be NEXT to that map's door trigger, not on it, or the player will bounce straight back
public class DoorScript extends Script {
    protected Supplier<Map> destinationMap;
    protected int spawnTileX, spawnTileY;
    protected Direction facingDirection;

    public DoorScript(Supplier<Map> destinationMap, int spawnTileX, int spawnTileY, Direction facingDirection) {
        this.destinationMap = destinationMap;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        this.facingDirection = facingDirection;
    }

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();
        scriptActions.add(new ChangeMapScriptAction(destinationMap, spawnTileX, spawnTileY, facingDirection));
        return scriptActions;
    }
}
