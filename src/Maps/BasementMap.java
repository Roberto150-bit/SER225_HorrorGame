package Maps;

import Level.*;
import Scripts.DoorScript;
import Tilesets.BasementTileset;
import Utils.Direction;


public class BasementMap extends Map {

    public BasementMap() {
        super("BasementMap.txt", new BasementTileset());
        this.playerStartPosition = getMapTile(8, 8).getLocation();
    }

    @Override
    public void loadScripts() {
        // door on the left wall stand next to it, face left, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        getMapTile(0, 7).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.RIGHT));
        getMapTile(0, 8).setInteractScript(new DoorScript(() -> new TestMap(), 22, 20, Direction.RIGHT));
    }

}
