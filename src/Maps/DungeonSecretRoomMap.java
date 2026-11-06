package Maps;

import Enemies.CreepyStickEnemy;
import Enemies.NecromancerBoss;
import EnhancedMapTiles.RitualFire;
import Level.EnhancedMapTile;
import Level.Map;
import Level.MapTile;
import Scripts.DoorScript;
import Tilesets.DungeonSecretRoomTileset;
import Utils.Direction;
import Utils.Point;
import java.util.ArrayList;

// The Necromancer's ritual chamber, behind the great door: the boss fight that ends Chapter 1.
// The Necromancer waits on the dais at the top of the room between two tall servants, lit by eight ritual candles.
// Stepping onto the ritual circle (or attacking) wakes it: the door seals behind the player, it speaks, and the fight begins.
// The door opens again once it's destroyed.
public class DungeonSecretRoomMap extends Map {

    private static final int BOSS_ID = 800;
    private static final int FIRST_GUARD_ID = 810;
    // the dais, just below the benches at the top of the room
    private static final int BOSS_TILE_X = 4;
    private static final int BOSS_TILE_Y = 3;
    // the servants stand on either side of the ritual circle
    private static final int[][] GUARD_TILES = {{1, 5}, {7, 5}};
    // crossing into this row (the bottom edge of the ritual circle) wakes the Necromancer
    private static final int AWAKEN_ROW = 7;

    // light: the candles let the player see further than in the rest of the dungeon, more once the ritual fire flares
    private static final float DORMANT_LIGHT_RADIUS = 300;
    private static final float FIGHT_LIGHT_RADIUS = 420;

    private static final float ARENA_ZOOM = 1.0f;
    // set when the Necromancer is beaten and Chapter 1 ends
    public static final String CHAPTER_COMPLETE_FLAG = "chapter1_complete";

    // set while the map is being built, so no initializers
    private NecromancerBoss boss;

    public DungeonSecretRoomMap() {
        super("DungeonSecretRoomMap.txt", new DungeonSecretRoomTileset());
        this.playerStartPosition = getMapTile(4, 8).getLocation();
        // the camera pulls back so the whole chamber fits on screen: the Necromancer fights from the dais and throws
        // fire skulls from range, so the player needs to see it (the rest of the dungeon is zoomed in to 1.5)
        getCamera().setZoom(ARENA_ZOOM);
    }

    private NecromancerBoss getOrCreateBoss() {
        if (boss == null) {
            boss = new NecromancerBoss(BOSS_ID, new Point(0, 0));
        }
        return boss;
    }
    /* 
    public ArrayList<Trigger> loadTriggers() {
        ArrayList<Trigger> triggers = new ArrayList<>();
        Trigger bossTrigger = new Trigger(300, 600, 100, 100, new BossEnterScript(), "HasEnteredRoom");
        bossTrigger.setExistenceFlag("HasEnteredRoom");
        triggers.add(bossTrigger);
        return triggers;
    }
    */
    @Override
    public ArrayList<EnhancedMapTile> loadEnhancedMapTiles() {
        ArrayList<EnhancedMapTile> enhancedMapTiles = new ArrayList<>();
        enhancedMapTiles.add(new RitualFire(getOrCreateBoss()));
        return enhancedMapTiles;
    }

    @Override
    public ArrayList<Level.NPC> loadNPCs() {
        ArrayList<Level.NPC> npcs = new ArrayList<>();
        float tileSize = getTileset().getScaledSpriteWidth();

        NecromancerBoss necromancer = getOrCreateBoss();
        MapTile bossTile = getMapTile(BOSS_TILE_X, BOSS_TILE_Y);
        necromancer.setBoundsLocation(
                bossTile.getX() + (tileSize - necromancer.getBounds().getWidth()) / 2f,
                bossTile.getY() + (tileSize - necromancer.getBounds().getHeight()) / 2f);
        necromancer.setAwakenLineY(AWAKEN_ROW * tileSize);
        npcs.add(necromancer);

        int id = FIRST_GUARD_ID;
        for (int[] tile : GUARD_TILES) {
            CreepyStickEnemy guard = new CreepyStickEnemy(id++, new Point(0, 0));
            MapTile guardTile = getMapTile(tile[0], tile[1]);
            guard.setBoundsLocation(
                    guardTile.getX() + (tileSize - guard.getBounds().getWidth()) / 2f,
                    guardTile.getY() + (tileSize - guard.getBounds().getHeight()) / 2f);
            guard.setStaysInRoom(true);
            necromancer.addGuard(guard);
            npcs.add(guard);
        }
        return npcs;
    }

    // once Chapter 1 is complete, the Necromancer stays beaten when the player comes back or restarts here
    @Override
    public void setFlagManager(Level.FlagManager flagManager) {
        super.setFlagManager(flagManager);
        if (flagManager != null && flagManager.isFlagSet(CHAPTER_COMPLETE_FLAG) && boss != null && !boss.isDefeated()) {
            boss.setAlreadyDefeated();
        }
    }

    public NecromancerBoss getBoss() {
        return boss;
    }

    // true from the moment the Necromancer wakes until it's destroyed
    private boolean isFightUnderway() {
        return boss != null && boss.isAwake() && !boss.isDefeated();
    }

    @Override
    public float getLightRadius() {
        return boss != null && boss.isAwake() ? FIGHT_LIGHT_RADIUS : DORMANT_LIGHT_RADIUS;
    }

    @Override
    public void loadScripts() {
        // the way back out, through the great door into the Final Seal room; sealed while the fight is on
        getMapTile(4, 10).setInteractScript(new DoorScript(() -> new DungeonRundownRoomMap(),
                DungeonRundownRoomMap.FROM_BOSS_ROOM_X, DungeonRundownRoomMap.FROM_BOSS_ROOM_Y, Direction.DOWN)
                .lockedUntil(() -> !isFightUnderway(),
                        () -> "The door has sealed itself shut.\nThere's no way out but through."));
    }
}
