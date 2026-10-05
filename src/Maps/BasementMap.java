package Maps;

import Level.*;
import Tilesets.BasementTileset;


public class BasementMap extends Map {

    public BasementMap() {
        super("BasementMap.txt", new BasementTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();
    }

}
