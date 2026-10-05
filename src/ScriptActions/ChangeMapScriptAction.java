package ScriptActions;

import java.util.function.Supplier;

import Level.GameListener;
import Level.Map;
import Level.ScriptState;
import Utils.Direction;
import Utils.Point;

// Script action that tells the game to load a different map and place the player at a given tile in it
// the new map is created through a Supplier (e.g. "() -> new HallwayMap()") so it is only built when the transition actually happens
public class ChangeMapScriptAction extends ScriptAction {
    protected Supplier<Map> mapFactory;
    protected int spawnTileX, spawnTileY;
    protected Direction facingDirection;

    public ChangeMapScriptAction(Supplier<Map> mapFactory, int spawnTileX, int spawnTileY, Direction facingDirection) {
        this.mapFactory = mapFactory;
        this.spawnTileX = spawnTileX;
        this.spawnTileY = spawnTileY;
        this.facingDirection = facingDirection;
    }

    @Override
    public ScriptState execute() {
        Map newMap = mapFactory.get();
        Point spawnPosition = newMap.getMapTile(spawnTileX, spawnTileY).getLocation();
        for (GameListener listener : map.getListeners()) {
            listener.onChangeMap(newMap, spawnPosition, facingDirection);
        }
        return ScriptState.COMPLETED;
    }
}
