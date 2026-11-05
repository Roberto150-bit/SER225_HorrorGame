package Maps;

import Enemies.NecromancerBoss;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Level.Trigger;
import Scripts.DoorScript;
import Scripts.DungeonMap.BossEnterScript;
import Tilesets.DungeonSecretRoomTileset;
import Utils.Direction;
import Utils.Point;
import java.util.ArrayList;

public class DungeonSecretRoomMap extends Map {

    // the first level's final boss waits in the middle of the room
    private static final int BOSS_ID = 800;
    private static final int BOSS_TILE_X = 4;
    private static final int BOSS_TILE_Y = 5;

    public DungeonSecretRoomMap() {
        super("DungeonSecretRoomMap.txt", new DungeonSecretRoomTileset());
        this.playerStartPosition = getMapTile(6, 4).getLocation();

        NecromancerBoss boss = new NecromancerBoss(BOSS_ID, new Point(0, 0));
        MapTile bossTile = getMapTile(BOSS_TILE_X, BOSS_TILE_Y);
        float tileSize = getTileset().getScaledSpriteWidth();
        boss.setBoundsLocation(
                bossTile.getX() + (tileSize - boss.getBounds().getWidth()) / 2f,
                bossTile.getY() + (tileSize - boss.getBounds().getHeight()) / 2f);
        addNPC(boss);
    }

    public ArrayList<Trigger> loadTriggers() {
        ArrayList<Trigger> triggers = new ArrayList<>();
        Trigger bossTrigger = new Trigger(300, 600, 100, 100, new BossEnterScript(), "HasEnteredRoom");
        bossTrigger.setExistenceFlag("HasEnteredRoom");
        triggers.add(bossTrigger);
        return triggers;
    }
    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();

        //nothing here yet, but this is where you would add any enhanced map tiles to the map. 

        return enhancedMapTiles;
    }


    @Override
    public void loadScripts() {
        // door on the right wall next to the stairs stand on the stairs at (12, 4), face right, and press E to go through
        // TestMap is a placeholder destination until the next room is built
        
        // HERE -------------------------------------------------------------
        getMapTile(4, 10).setInteractScript(new DoorScript(DungeonRoomSetup::hallway, 5, 4, Direction.LEFT));
    }
}