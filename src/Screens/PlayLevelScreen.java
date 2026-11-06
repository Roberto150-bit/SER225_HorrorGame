package Screens;

import Enemies.Ghost;
import Enemies.NecromancerBoss;
import Engine.GraphicsHandler;
import Engine.Jumpscare;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.MirrorCutscene;
import Engine.Mouse;
import Engine.MusicManager;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.*;
import Lighting.DarknessManager;
import Maps.BasementMap;
import Maps.DungeonSecretRoomMap;
import Players.DeanPlayer;
import Puzzles.PuzzleManager;
import Scripts.ValeIntroScript;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// This class is for when the RPG game is actually being played
public class PlayLevelScreen extends Screen implements GameListener {
    private static final String GAMEPLAY_MUSIC = "Resources/Audios/Music/FirstBackgroundMusic.wav";
    // music for each situation, with volumes picked so the music sits under the sound effects
    // (the exploration track is mastered much louder than the others, so it plays quieter)
    private static final String EXPLORATION_MUSIC = "Resources/Audios/Music/FirstBackgroundMusic.wav";
    private static final float EXPLORATION_VOLUME = 0.45f;
    private static final String GHOST_CHASE_MUSIC = "Resources/Audios/Music/Whisper of the Abyss.wav";
    private static final float GHOST_CHASE_VOLUME = 0.6f;
    private static final String BOSS_MUSIC = "Resources/Audios/horrorthriller.wav";
    private static final float BOSS_VOLUME = 0.8f;
    // a low drone in the ritual chamber while the Necromancer waits
    private static final String BOSS_ROOM_AMBIENCE = "Resources/Audios/Music/BackgroundLowNoise.wav";
    private static final float BOSS_ROOM_AMBIENCE_VOLUME = 1f;
    private static final String ENDING_MUSIC = "Resources/Audios/Music/Where My Home Is.wav";
    private static final float ENDING_VOLUME = 0.8f;

    // the latest checkpoint, restarted from after dying
    private Checkpoint checkpoint;
    private static final int CHECKPOINT_MESSAGE_FRAMES = 150;
    private static final Font CHECKPOINT_FONT = new Font("Serif", Font.BOLD, 22);
    private int checkpointMessageTimer = 0;
    private String checkpointMessage = "";
    // frames of safety after restarting, so an enemy waiting nearby can't hit the player straight away
    private static final int RESTART_INVINCIBLE_FRAMES = 120;

    private static final Font BOSS_NAME_FONT = new Font("Serif", Font.BOLD, 18);
    private static final Font OBJECTIVE_FONT = new Font("Serif", Font.BOLD, 18);

    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    
    // Retains one instance of each room during the playthrough.
    protected final HashMap<String, Map> loadedRooms = new HashMap<>();
    // Tracks rooms whose scripts have already been initialized.
    protected final HashSet<Map> preloadedRooms = new HashSet<>();
    
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected WinScreen winScreen;
    protected LoseScreen loseScreen;
    protected PauseMenu pauseMenu;
    protected KeyLocker keyLocker = new KeyLocker();
    protected FlagManager flagManager;

    // map transition (door) state -- the screen fades to black, swaps maps, then fades back in
    protected static final int FADE_SPEED = 15; // alpha change per frame (0-255)
    protected TransitionPhase transitionPhase = TransitionPhase.NONE;
    protected int fadeAlpha = 0;
    protected Map pendingMap;
    protected Point pendingSpawnPosition;
    protected Direction pendingFacingDirection;
    // something to do while the screen is black, instead of changing rooms (see onFadeAndChange)
    private Runnable pendingFadeChange;
    // frames left of screen shake
    private int shakeTimer = 0;
    private final java.util.Random shakeRandom = new java.util.Random();
    // Distance of each pursuing ghost when Dean enters a doorway.
    private final HashMap<Ghost, Float> pursuingGhostDistances = new HashMap<>();

    // Ghosts waiting to enter another room.
    private final ArrayList<GhostArrival> ghostArrivals = new ArrayList<>();

    // last spot the player stood that wasn't inside a wall, used to rescue them if something pushes them into one
    private float lastSafePlayerX, lastSafePlayerY;

    // Stores the information needed for a delayed ghost arrival.
    private static class GhostArrival {
        Ghost ghost;
        Map destination;
        float x;
        float y;
        int framesRemaining;

        GhostArrival(Ghost ghost, Map destination, float x, float y, int frames) {
            this.ghost = ghost;
            this.destination = destination;
            this.x = x;
            this.y = y;
            this.framesRemaining = frames;
        }
    }
    private DarknessManager darknessManager;
    
    // Displays the player's current level and XP.
    private final LevelProgressHUD levelProgressHUD = new LevelProgressHUD();

    // Flashes the screen red when the player is hit.
    private final HealthHUD healthHUD = new HealthHUD();

    public PlayLevelScreen(ScreenCoordinator screenCoordinator) {
        this.screenCoordinator = screenCoordinator;
    }

    public void initialize() {
        // setup state
        flagManager = new FlagManager();
        flagManager.addFlag("hasLostBall", false);
        flagManager.addFlag("hasTalkedToWalrus", false);
        flagManager.addFlag("hasTalkedToDinosaur", false);
        flagManager.addFlag("hasFoundBall", false);

        // define/setup map -------------------------------------------------------------------------------
        //map = new TestMap();

        // a new game starts with every door locked again and a new code for the final seal
        PuzzleManager.reset();
        Ghost.resetDrops();
        Jumpscare.stop();
        MirrorCutscene.stop();
        MirrorCutscene.resetSeen();
        NecromancerBoss.resetIntro();
        // a new game starts back at level 1 with no XP
        PlayerProgressManager.reset();
        // full health every new game, including after dying and starting again from the main menu
        Player.setHealth(Player.MAX_HEALTH);

        map = new BasementMap();
        MusicManager.play(GAMEPLAY_MUSIC, 1.6f, true);

        loadedRooms.clear();
        preloadedRooms.clear();
        // Reset ghost pursuit when starting a new playthrough.
        ghostArrivals.clear();
        pursuingGhostDistances.clear();
        
        loadedRooms.put(map.getClass().getName(), map);
        map.setFlagManager(flagManager);

        // setup player
        // player = new Cat(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
        player = new DeanPlayer(map.getPlayerStartPosition().x, map.getPlayerStartPosition().y);
        player.setMap(map);
        playLevelScreenState = PlayLevelScreenState.RUNNING;
        player.setFacingDirection(Direction.LEFT);
        lastSafePlayerX = player.getX();
        lastSafePlayerY = player.getY();

        map.setPlayer(player);

        // let pieces of map know which button to listen for as the "interact" button
        //map.getTextbox().setInteractKey(player.getInteractKey()); //--------------------------------------------------------------

        // add this screen as a "game listener" so other areas of the game that don't normally have direct access to it (such as scripts) can "signal" to have it do something
        // this is used in the "onWin" method -- a script signals to this class that the game has been won by calling its "onWin" method
        map.addListener(this);

        // preloads all scripts ahead of time rather than loading them dynamically
        // both are supported, however preloading is recommended
        map.preloadScripts();
        preloadedRooms.add(map);

        // Begin Mrs. Vale's introduction automatically.
        map.setActiveScript(new ValeIntroScript());
        
        darknessManager = new DarknessManager(180.0f);
    
        winScreen = new WinScreen(this);
        loseScreen = new LoseScreen(this);
        pauseMenu = new PauseMenu(this);
        // ESC may still be held from a previous menu, so it has to be released before it can pause
        keyLocker.lockKey(Key.ESC);

        transitionPhase = TransitionPhase.NONE;
        fadeAlpha = 0;
        pendingMap = null;
        pendingFadeChange = null;
        shakeTimer = 0;

        // the start of the chapter is the first checkpoint (it doesn't need announcing)
        checkpoint = Checkpoint.capture(map, player, flagManager);
        checkpointMessageTimer = 0;
        updateMusic();
    }

    public void update() {
        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the platformer level going
            case RUNNING:
                if (escPressed() && transitionPhase == TransitionPhase.NONE) {
                    pauseGame();
                    break;
                }
                // everything freezes while a jumpscare is on screen
                if (Jumpscare.isPlaying()) {
                    Jumpscare.update();
                    // jumpscares only play on death, so cut straight from the creature's face to the lose screen
                    if (!Jumpscare.isPlaying() && player.isDead()) {
                        playLevelScreenState = PlayLevelScreenState.LOSE;
                        loseScreen.open();
                    }
                    break;
                }

                // everything freezes while the mirror cutscene plays
                if (MirrorCutscene.isPlaying()) {
                    MirrorCutscene.update();
                    break;
                }

                updateGhostArrivals();
                if (transitionPhase != TransitionPhase.NONE) {
                    updateTransition();
                    break;
                }
                // Freeze Dean while reading the journal.
                if (!map.isBookOpen()) {
                    player.update();
                }
                map.update(player);
                keepPlayerOutOfWalls();

                darknessManager.update();
                if (checkpointMessageTimer > 0) {
                    checkpointMessageTimer--;
                }
                if (shakeTimer > 0) {
                    shakeTimer--;
                }
                // show the lose screen after the player's damage animation finishes
                if (player.isDead() && !player.isHurt()) {
                    playLevelScreenState = PlayLevelScreenState.LOSE;
                    loseScreen.open();
                }
                break;
            case PAUSED:
                if (escPressed()) {
                    resumeGame();
                    break;
                }
                pauseMenu.update();
                break;
            // if level has been completed, bring up level cleared screen
            case LEVEL_COMPLETED:
                winScreen.update();
                break;
            case LOSE:
                loseScreen.update();
                break;
        }
        updateMusic();
    }

    // true only on the frame ESC goes down
    private boolean escPressed() {
        if (Keyboard.isKeyUp(Key.ESC)) {
            keyLocker.unlockKey(Key.ESC);
            return false;
        }
        if (!keyLocker.isKeyLocked(Key.ESC)) {
            keyLocker.lockKey(Key.ESC);
            return true;
        }
        return false;
    }

    public void pauseGame() {
        playLevelScreenState = PlayLevelScreenState.PAUSED;
        pauseMenu.open();
    }

    public void resumeGame() {
        playLevelScreenState = PlayLevelScreenState.RUNNING;

        // Restore gameplay music after returning from the main menu.
        updateMusic();

        // puzzles use the mouse, so only hide the cursor if one isn't open
        if (!map.getPuzzleUI().isOpen()) {
            Mouse.hideCursor();
        }
    }

    // picks the track for what's happening right now: the boss fight, a nearby ghost, or exploring
    private void updateMusic() {
        if (map == null || player == null) {
            return;
        }

        if (playLevelScreenState == PlayLevelScreenState.PAUSED || playLevelScreenState == PlayLevelScreenState.LOSE) {
            return;
        }

        String targetTrack = getRoomMusic();
        String currentTrack = MusicManager.getCurrentFile();
        if (targetTrack == null) {
            if (currentTrack != null) {
                MusicManager.stop();
            }
        } else if (!targetTrack.equals(currentTrack)) {
            MusicManager.play(targetTrack, getRoomMusicVolume(targetTrack), true);
        }
    }

    private String getRoomMusic() {
        switch (playLevelScreenState) {
            case LEVEL_COMPLETED:
                return ENDING_MUSIC;
            case RUNNING:
                break;
            default:
                return null;
        }

        if (map instanceof DungeonSecretRoomMap && flagManager.isFlagSet(DungeonSecretRoomMap.CHAPTER_COMPLETE_FLAG)) {
            // the quiet after Chapter 1, while the player looks around the Necromancer's room
            return ENDING_MUSIC;
        }

        if (isGhostNearby()) {
            return GHOST_CHASE_MUSIC;
        }

        NecromancerBoss boss = getBoss();
        if (boss != null && boss.isDefeated()) {
            // the encounter is over: the boss music stops for the closing dialogue
            return null;
        } else if (boss != null && boss.isAwake()) {
            return BOSS_MUSIC;
        } else if (boss != null) {
            return BOSS_ROOM_AMBIENCE;
        } else {
            return EXPLORATION_MUSIC;
        }
    }

    private float getRoomMusicVolume(String track) {
        if (ENDING_MUSIC.equals(track)) {
            return ENDING_VOLUME;
        } else if (GHOST_CHASE_MUSIC.equals(track)) {
            return GHOST_CHASE_VOLUME;
        } else if (BOSS_MUSIC.equals(track)) {
            return BOSS_VOLUME;
        } else if (BOSS_ROOM_AMBIENCE.equals(track)) {
            return BOSS_ROOM_AMBIENCE_VOLUME;
        } else if (EXPLORATION_MUSIC.equals(track)) {
            return EXPLORATION_VOLUME;
        }
        throw new IllegalArgumentException("Unknown room music track: " + track);
    }

    private boolean isGhostNearby() {
        for (NPC npc : map.getNPCs()) {
            if (npc instanceof Ghost && ((Ghost) npc).isWithinDetectionRadius(player)) {
                return true;
            }
        }
        return false;
    }

    // the Necromancer, if the current room is its room
    private NecromancerBoss getBoss() {
        for (NPC npc : map.getNPCs()) {
            if (npc instanceof NecromancerBoss) {
                return (NecromancerBoss) npc;
            }
        }
        return null;
    }

    // remembers the game as it is right now as the place to restart from
    private void saveCheckpoint() {
        checkpoint = Checkpoint.capture(map, player, flagManager);
        checkpointMessage = "Checkpoint reached";
        checkpointMessageTimer = CHECKPOINT_MESSAGE_FRAMES;
    }

    // solving a puzzle for the first time is a checkpoint
    @Override
    public void onCheckpointReached() {
        saveCheckpoint();
    }

    // after dying: back to the latest checkpoint with full health, and everything earned up to it
    public void restartFromCheckpoint() {
        if (checkpoint == null) {
            resetLevel();
            return;
        }
        Jumpscare.stop();
        MirrorCutscene.stop();
        checkpoint.restoreProgress();

        flagManager = checkpoint.copyFlags();
        loadedRooms.clear();
        preloadedRooms.clear();
        ghostArrivals.clear();
        pursuingGhostDistances.clear();

        // a fresh copy of the room, with the inventory and journal as they were
        map = checkpoint.createRoom();
        loadedRooms.put(map.getClass().getName(), map);
        map.setFlagManager(flagManager);
        map.setHotbarUI(checkpoint.copyHotbar());
        map.setUI(checkpoint.copyJournal());

        player = new DeanPlayer(checkpoint.getPlayerX(), checkpoint.getPlayerY());
        player.setMap(map);
        player.setFacingDirection(checkpoint.getFacingDirection());
        player.grantInvincibility(RESTART_INVINCIBLE_FRAMES);
        map.setPlayer(player);
        map.addListener(this);
        map.preloadScripts();
        preloadedRooms.add(map);
        clearEnemiesNear(player);

        lastSafePlayerX = player.getX();
        lastSafePlayerY = player.getY();
        transitionPhase = TransitionPhase.NONE;
        fadeAlpha = 0;
        pendingMap = null;
        pendingFadeChange = null;
        shakeTimer = 0;
        keyLocker.lockKey(Key.ESC);

        playLevelScreenState = PlayLevelScreenState.RUNNING;
        map.update(player); // settles the camera on the player before the first frame is drawn
        checkpointMessage = "Restarted from checkpoint";
        checkpointMessageTimer = CHECKPOINT_MESSAGE_FRAMES;
        updateMusic();
    }

    // enemies are placed at random when a room is built, so one could land right next to the restart spot
    // (the boss is left alone, it's what the player came back to fight)
    private void clearEnemiesNear(Player player) {
        float safeDistance = map.getTileset().getScaledSpriteWidth() * 2.5f;
        Point center = player.getCenter();
        map.getNPCs().removeIf(npc -> {
            if (!(npc instanceof Ghost) || npc instanceof NecromancerBoss) {
                return false;
            }
            float dx = npc.getBounds().getX() + npc.getBounds().getWidth() / 2f - center.x;
            float dy = npc.getBounds().getY() + npc.getBounds().getHeight() / 2f - center.y;
            return dx * dx + dy * dy < safeDistance * safeDistance;
        });
    }

    @Override
    public void onWin() {
        // Chapter 1 is complete: the card goes up over the game, and the player can keep going from here
        flagManager.addFlag(DungeonSecretRoomMap.CHAPTER_COMPLETE_FLAG, true);
        playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
        winScreen.open();
    }

    // "Continue" on the Chapter 1 card: back to the game in the Necromancer's room, with a checkpoint here
    public void continueAfterChapter() {
        playLevelScreenState = PlayLevelScreenState.RUNNING;
        Mouse.hideCursor();
        keyLocker.lockKey(Key.ESC);
        saveCheckpoint();
    }

    // a script (e.g. DoorScript) has asked to move the player to a different map
    // the swap is not done immediately since this is called in the middle of the old map's update -- it happens once the screen has faded to black
    @Override
    public void onChangeMap(Map newMap, Point spawnPosition, Direction facingDirection) {
        if (transitionPhase != TransitionPhase.NONE) {
            return;
        }

        // Capture pursuing ghosts at the moment Dean uses the doorway.
        pursuingGhostDistances.clear();

        for (NPC npc : map.getNPCs()) {
            if (!(npc instanceof Ghost)) {
                continue;
            }

            Ghost ghost = (Ghost) npc;

            if (!ghost.canFollowThroughDoor(player)) {
                continue;
            }

            float ghostX = ghost.getBounds().getX()
                    + ghost.getBounds().getWidth() / 2f;
            float ghostY = ghost.getBounds().getY()
                    + ghost.getBounds().getHeight() / 2f;

            float playerX = player.getBounds().getX()
                    + player.getBounds().getWidth() / 2f;
            float playerY = player.getBounds().getY()
                    + player.getBounds().getHeight() / 2f;

            float dx = playerX - ghostX;
            float dy = playerY - ghostY;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            pursuingGhostDistances.put(ghost, distance);
        }

        // Identify the destination room.
        String roomId = newMap.getClass().getName();

        // Reuse an existing room or register it on first entry.
        if (loadedRooms.containsKey(roomId)) {
            pendingMap = loadedRooms.get(roomId);
        } else {
            loadedRooms.put(roomId, newMap);
            pendingMap = newMap;
        }

        pendingSpawnPosition = spawnPosition;
        pendingFacingDirection = facingDirection;
        transitionPhase = TransitionPhase.FADING_OUT;
    }

    @Override
    public void onFadeAndChange(Runnable change) {
        if (transitionPhase != TransitionPhase.NONE) {
            change.run();
            return;
        }
        pendingFadeChange = change;
        transitionPhase = TransitionPhase.FADING_OUT;
    }

    @Override
    public void onScreenShake(int frames) {
        shakeTimer = Math.max(shakeTimer, frames);
    }

    // player is frozen during the transition; the map keeps updating so the door script can finish and the camera can settle
    private void updateTransition() {
        if (transitionPhase == TransitionPhase.FADING_OUT) {
            map.update(player);
            fadeAlpha = Math.min(255, fadeAlpha + FADE_SPEED);
            if (fadeAlpha == 255) {
                if (pendingFadeChange != null) {
                    Runnable change = pendingFadeChange;
                    pendingFadeChange = null;
                    change.run();
                    map.update(player);
                } else {
                    swapToPendingMap();
                }
                transitionPhase = TransitionPhase.FADING_IN;
            }
        }
        else if (transitionPhase == TransitionPhase.FADING_IN) {
            map.update(player);
            fadeAlpha = Math.max(0, fadeAlpha - FADE_SPEED);
            if (fadeAlpha == 0) {
                transitionPhase = TransitionPhase.NONE;
            }
        }
    }

    private void swapToPendingMap() {
        if (pendingMap == null) {
            return;
        }

        // Identify ghosts following Dean before changing rooms.
        ArrayList<Ghost> followingGhosts = new ArrayList<>();

        for (NPC npc : new ArrayList<>(map.getNPCs())) {
            if (npc instanceof Ghost) {
                Ghost ghost = (Ghost) npc;
                boolean canFollow = ghost.canFollowThroughDoor(player);

                if (canFollow) {
                    followingGhosts.add(ghost);
                }
            }
        }

        // Preserve shared game state.
        pendingMap.setFlagManager(flagManager);
        pendingMap.setHotbarUI(map.getHotbarUI());
        pendingMap.setUI(map.getUI());
        pendingMap.setBookOpen(false);
        pendingMap.setPlayer(player);

        // Avoid registering duplicate listeners.
        if (!pendingMap.getListeners().contains(this)) {
            pendingMap.addListener(this);
        }

        for (java.util.Map.Entry<Ghost, Float> entry : pursuingGhostDistances.entrySet()) {

            Ghost ghost = entry.getKey();
            float distance = entry.getValue();

            if (!map.getNPCs().contains(ghost)) {
                continue;
            }

            // Greater distance means a longer arrival delay.
            int delay = Math.max(12,
                    Math.min(120, Math.round(distance / 2f)));

            // Use Dean's destination doorway as the arrival reference.
            float ghostX = pendingSpawnPosition.x;
            float ghostY = pendingSpawnPosition.y;

            float tileW = pendingMap.getTileset().getScaledSpriteWidth();
            float tileH = pendingMap.getTileset().getScaledSpriteHeight();

            switch (pendingFacingDirection) {
                case LEFT:
                    ghostX += tileW * 0.55f;
                    break;
                case RIGHT:
                    ghostX -= tileW * 0.55f;
                    break;
                case UP:
                    ghostY += tileH * 0.55f;
                    break;
                case DOWN:
                    ghostY -= tileH * 0.55f;
                    break;
                case NONE:
                    break;
            }

            // Remove from the old room while awaiting arrival.
            map.getNPCs().remove(ghost);

            ghostArrivals.add(new GhostArrival(
                    ghost, pendingMap, ghostX, ghostY, delay
            ));
        }

        pursuingGhostDistances.clear();

        // Move Dean to the destination room.
        player.setMap(pendingMap);
        player.setFacingDirection(pendingFacingDirection);
        placePlayerAtSpawn(pendingMap, pendingSpawnPosition);
        lastSafePlayerX = player.getX();
        lastSafePlayerY = player.getY();
        player.setPlayerState(PlayerState.STANDING);

        // Initialize scripts only once per room.
        if (preloadedRooms.add(pendingMap)) {
            pendingMap.preloadScripts();
        }

        map = pendingMap;

        // Clear completed transition references.
        pendingMap = null;
        pendingSpawnPosition = null;
        pendingFacingDirection = null;

        // Update the new room and camera.
        map.update(player);

        // walking into the Necromancer's room is a checkpoint, so a lost fight can be tried again from the door
        if (map instanceof DungeonSecretRoomMap) {
            NecromancerBoss boss = getBoss();
            if (boss != null && !boss.isDefeated()) {
                saveCheckpoint();
            }
        }
    }


    // The spawn position is the top-left of the spawn tile, but Dean's hitbox sits well inside his sprite.
    // Putting the sprite's corner on the tile could leave his hitbox inside a wall or off the map, and moving from
    // there pushed him through the wall and out of bounds. Instead, center the hitbox on the spawn tile,
    // and if that spot is blocked, use the closest open spot nearby.
    private void placePlayerAtSpawn(Map room, Point spawnTilePosition) {
        float hitboxOffsetX = player.getBounds().getX() - player.getX();
        float hitboxOffsetY = player.getBounds().getY() - player.getY();
        float hitboxWidth = player.getBounds().getWidth();
        float hitboxHeight = player.getBounds().getHeight();

        float startX = spawnTilePosition.x + (room.getTileset().getScaledSpriteWidth() - hitboxWidth) / 2f;
        float startY = spawnTilePosition.y + (room.getTileset().getScaledSpriteHeight() - hitboxHeight) / 2f;

        float hitboxX = startX;
        float hitboxY = startY;
        boolean foundSpot = false;

        // Search outward from the spawn tile.
        for (int radius = 0; radius <= 128 && !foundSpot; radius += 4) {
            for (int dx = -radius; dx <= radius && !foundSpot; dx += 4) {
                for (int dy = -radius; dy <= radius; dy += 4) {
                    if (isOpenArea(room, startX + dx, startY + dy, hitboxWidth, hitboxHeight)) {
                        hitboxX = startX + dx;
                        hitboxY = startY + dy;
                        foundSpot = true;
                        break;
                    }
                }
            }
        }

        player.setLocation(hitboxX - hitboxOffsetX, hitboxY - hitboxOffsetY);
    }

    // walls are checked with the same bounds the collision code uses, so any spot the player can normally walk to counts as safe
    private void keepPlayerOutOfWalls() {
        if (isInsideWall(map, player.getBounds())) {
            player.setLocation(lastSafePlayerX, lastSafePlayerY);
        } else {
            lastSafePlayerX = player.getX();
            lastSafePlayerY = player.getY();
        }
    }

    private boolean isInsideWall(Map room, GameObject.Rectangle bounds) {
        if (bounds.getX() < 0 || bounds.getY() < 0
                || bounds.getX() + bounds.getWidth() > room.getWidthPixels()
                || bounds.getY() + bounds.getHeight() > room.getHeightPixels()) {
            return true;
        }
        for (MapTile tile : room.getMapTiles()) {
            if (tile.getTileType() == TileType.NOT_PASSABLE && tile.intersects(bounds)) {
                return true;
            }
        }
        return false;
    }

    private void updateGhostArrivals() {
        for (int i = ghostArrivals.size() - 1; i >= 0; i--) {
            GhostArrival arrival = ghostArrivals.get(i);

            // Wait until the ghost is ready to enter.
            if (arrival.framesRemaining > 0) {
                arrival.framesRemaining--;
                continue;
            }

            float ghostWidth = arrival.ghost.getBounds().getWidth();
            float ghostHeight = arrival.ghost.getBounds().getHeight();

            float maxX = Math.max(0,
                    arrival.destination.getWidthPixels() - ghostWidth);
            float maxY = Math.max(0,
                    arrival.destination.getHeightPixels() - ghostHeight);

            float startX = Math.max(0, Math.min(arrival.x, maxX));
            float startY = Math.max(0, Math.min(arrival.y, maxY));

            boolean foundSpawn = false;
            float safeX = startX;
            float safeY = startY;

            // Search outward from the doorway.
            for (int radius = 0; radius <= 128 && !foundSpawn; radius += 16) {
                for (int dx = -radius; dx <= radius && !foundSpawn; dx += 16) {
                    for (int dy = -radius; dy <= radius; dy += 16) {
                        float candidateX = startX + dx;
                        float candidateY = startY + dy;

                        if (isValidGhostSpawn(
                                arrival.destination,
                                arrival.ghost,
                                candidateX,
                                candidateY)) {

                            safeX = candidateX;
                            safeY = candidateY;
                            foundSpawn = true;
                            break;
                        }
                    }
                }
            }

            if (!foundSpawn) {
                // Don't spawn inside an obstacle.
                // Try again on the next update.
                continue;
            }

            // the open spot was checked for the ghost's bounds, so place it by its bounds
            arrival.ghost.setBoundsLocation(safeX, safeY);

            // Preserve the existing ghost instance and animations.
            if (!arrival.destination.getNPCs().contains(arrival.ghost)) {
                arrival.destination.addNPC(arrival.ghost);
            }

            ghostArrivals.remove(i);
        }
    }

        
    private boolean isValidGhostSpawn(Map room, Ghost ghost, float x, float y) {
        return isOpenArea(room, x, y, ghost.getBounds().getWidth(), ghost.getBounds().getHeight());
    }

    // true if a box at (x, y) of the given size is on the map and not overlapping anything solid
    private boolean isOpenArea(Map room, float x, float y, float width, float height) {

        // The area must remain within the map boundaries.
        if (x < 0 || y < 0
                || x + width > room.getWidthPixels()
                || y + height > room.getHeightPixels()) {
            return false;
        }

        // Check the four corners against solid map tiles.
        float[] xs = {x, x + width - 1};
        float[] ys = {y, y + height - 1};

        for (float px : xs) {
            for (float py : ys) {
                MapTile tile = room.getTileByPosition(px, py);

                if (tile == null ||
                        tile.getTileType() == TileType.NOT_PASSABLE) {
                    return false;
                }
            }
        }

        // Check solid furniture and other enhanced map tiles.
        for (EnhancedMapTile object : room.getEnhancedMapTiles()) {
            if (object.getTileType() != TileType.NOT_PASSABLE
                    || !object.exists()) {
                continue;
            }

            float ox = object.getBounds().getX();
            float oy = object.getBounds().getY();
            float ow = object.getBounds().getWidth();
            float oh = object.getBounds().getHeight();

            if (x < ox + ow && x + width > ox
                    && y < oy + oh && y + height > oy) {
                return false;
            }
        }

        return true;
    }


    public void draw(GraphicsHandler graphicsHandler) {
        // based on screen state, draw appropriate graphics
        switch (playLevelScreenState) {
            case RUNNING:
                drawGame(graphicsHandler);
                break;
            case PAUSED:
                drawGame(graphicsHandler);
                pauseMenu.draw(graphicsHandler);
                break;
            case LEVEL_COMPLETED:
                drawGame(graphicsHandler);
                winScreen.draw(graphicsHandler);
                break;
            case LOSE:
                loseScreen.draw(graphicsHandler);
                break;
        }
    }

    private void drawGame(GraphicsHandler graphicsHandler) {
        // the world (not the HUD) shakes while the ground rumbles
        java.awt.geom.AffineTransform unshaken = graphicsHandler.getGraphics().getTransform();
        if (shakeTimer > 0 && playLevelScreenState == PlayLevelScreenState.RUNNING) {
            int strength = Math.min(6, 2 + shakeTimer / 20);
            graphicsHandler.getGraphics().translate(shakeRandom.nextInt(strength * 2 + 1) - strength,
                    shakeRandom.nextInt(strength * 2 + 1) - strength);
        }
        map.drawWorld(player, graphicsHandler);
        
        if (!map.isBookOpen()) {
            darknessManager.setTargetRadius(map.getLightRadius());
            darknessManager.draw(
                    graphicsHandler,
                    player,
                    map.getCamera()
            );
        } else {
            darknessManager.setTargetRadius(500);//originally 380 but changed to 500 to make it easier to see the book
            darknessManager.draw(
                    graphicsHandler,
                    player,
                    map.getCamera()
            );
        }

        // candles, seals and fireballs glow through the dark
        map.drawAboveDarkness(graphicsHandler);
        graphicsHandler.getGraphics().setTransform(unshaken);

        map.drawUI(graphicsHandler);

        // the HUD stays out of the way while a puzzle is open (the hotbar and hearts are hidden by map.drawUI)
        if (!map.getPuzzleUI().isOpen()) {
            // Display the XP bar above the game world.
            levelProgressHUD.draw(graphicsHandler);
            drawBossHealthBar(graphicsHandler);
            drawObjective(graphicsHandler);
        }
        drawCheckpointMessage(graphicsHandler);

        // the journal and dialogue go over the HUD
        if (playLevelScreenState == PlayLevelScreenState.RUNNING) {
            map.drawOverlays(graphicsHandler);
        }

        healthHUD.draw(graphicsHandler, player);

        // Render the fade during room transitions.
        if (fadeAlpha > 0) {
            graphicsHandler.drawFilledRectangle(
                0,
                0,
                graphicsHandler.getGraphics().getClipBounds().width,
                graphicsHandler.getGraphics().getClipBounds().height,
                new Color(0, 0, 0, fadeAlpha)
            );
        }

        Jumpscare.draw(graphicsHandler);
        MirrorCutscene.draw(graphicsHandler);
    }

    // the Necromancer's name and health across the top of the screen while the fight is on
    private void drawBossHealthBar(GraphicsHandler graphicsHandler) {
        NecromancerBoss boss = getBoss();
        if (boss == null || !boss.isAwake() || boss.isDefeated()) {
            return;
        }
        int width = 360;
        int height = 14;
        int x = (ScreenManager.getScreenWidth() - width) / 2;
        int y = 34;
        String name = "THE NECROMANCER";
        int nameWidth = graphicsHandler.getGraphics().getFontMetrics(BOSS_NAME_FONT).stringWidth(name);
        graphicsHandler.drawStringWithOutline(name, (ScreenManager.getScreenWidth() - nameWidth) / 2, y - 8,
                BOSS_NAME_FONT, new Color(230, 200, 190), Color.BLACK, 3);

        graphicsHandler.drawFilledRectangle(x, y, width, height, new Color(40, 10, 14, 220));
        int filled = Math.round(width * boss.getHealth() / (float) boss.getMaxHealth());
        graphicsHandler.drawFilledRectangle(x, y, filled, height, new Color(170, 20, 30));
        // a notch for every point of health, so each hit is easy to see
        for (int i = 1; i < boss.getMaxHealth(); i++) {
            int notchX = x + Math.round(width * i / (float) boss.getMaxHealth());
            graphicsHandler.drawFilledRectangle(notchX, y, 1, height, new Color(0, 0, 0, 120));
        }
        graphicsHandler.drawRectangle(x, y, width, height, new Color(200, 165, 110), 2);
    }

    // the room's current goal, e.g. how many of the great door's guardians are left
    private void drawObjective(GraphicsHandler graphicsHandler) {
        String objective = map.getObjectiveText();
        if (objective == null || map.getTextbox().isActive()) {
            return;
        }
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(OBJECTIVE_FONT).stringWidth(objective);
        graphicsHandler.drawStringWithOutline(objective, (ScreenManager.getScreenWidth() - textWidth) / 2, 30,
                OBJECTIVE_FONT, new Color(225, 120, 110), Color.BLACK, 3);
    }

    // "Checkpoint reached" (or "Restarted from checkpoint") fades out near the top of the screen
    private void drawCheckpointMessage(GraphicsHandler graphicsHandler) {
        if (checkpointMessageTimer <= 0) {
            return;
        }
        // fully visible for most of the time, fading over the last 40 frames
        int alpha = Math.min(255, checkpointMessageTimer * 255 / 40);
        int textWidth = graphicsHandler.getGraphics().getFontMetrics(CHECKPOINT_FONT).stringWidth(checkpointMessage);
        graphicsHandler.drawStringWithOutline(checkpointMessage, (ScreenManager.getScreenWidth() - textWidth) / 2, 82,
                CHECKPOINT_FONT, new Color(230, 200, 140, alpha), new Color(0, 0, 0, alpha), 3);
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    public void resetLevel() {
        initialize();
    }

    public void goBackToMenu() {
        Mouse.hideCursor();
        screenCoordinator.setGameState(GameState.MENU);
    }

    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, PAUSED, LEVEL_COMPLETED, LOSE
    }

    private enum TransitionPhase {
        NONE, FADING_OUT, FADING_IN
    }
}
