package MapEditor;

import Level.Map;
import Maps.TestMap;
import Maps.TitleScreenMap;
import Maps.BasementMap;
import Maps.DungeonCellMap;
import Maps.DungeonHallwayMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("TestMap");
            add("TitleScreen");
            add("BasementMap");
            add("DungeonCellMap");
            add("DungeonHallwayMap");
            add("DungeonWaterRoomMap");
            add("DungeonBasicRoomMap");
            add("DungeonSecretRoomMap");
            add("DungeonLibraryMap");
            
        }};
    }

    public static Map getMapByName(String mapName) {
        switch(mapName) {
            case "TestMap":
                return new TestMap();
            case "TitleScreen":
                return new TitleScreenMap();
            case "BasementMap":
                return new BasementMap();
            case "DungeonCellMap":
                return new DungeonCellMap();
            case "DungeonHallwayMap":
                return new DungeonHallwayMap();
            case "DungeonWaterRoomMap":
                return new Maps.DungeonWaterRoomMap();
            case "DungeonBasicRoomMap":
                return new Maps.DungeonBasicRoomMap();
            case "DungeonSecretRoomMap":
                return new Maps.DungeonSecretRoomMap();
            case "DungeonLibraryMap":
                return new Maps.DungeonLibraryMap();
            default:
                throw new RuntimeException("Unrecognized map name");
        }
    }
}
