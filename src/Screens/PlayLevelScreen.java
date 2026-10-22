package Screens;

import Engine.GraphicsHandler;
import Engine.Key;
import Engine.KeyLocker;
import Engine.Keyboard;
import Engine.Mouse;
import Engine.MusicManager;
import Engine.Screen;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.*;
import Lighting.DarknessManager;
import Maps.BasementMap;
import Players.DeanPlayer;
import Puzzles.PuzzleManager;
import Utils.Direction;
import Utils.Point;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import Enemies.Ghost;
import java.util.ArrayList;

// This class is for when the RPG game is actually being played
public class PlayLevelScreen extends Screen implements GameListener {
    private static final String GHOST_CHASE_MUSIC = "Resources/Audios/Music/Whisper of the Abyss.wav";

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
    // Distance of each pursuing ghost when Dean enters a doorway.
    private final HashMap<Ghost, Float> pursuingGhostDistances = new HashMap<>();

    // Ghosts waiting to enter another room.
    private final ArrayList<GhostArrival> ghostArrivals = new ArrayList<>();
    private boolean ghostChaseMusicPlaying;
    private String musicBeforeGhostChase;

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

        map = new BasementMap();
        MusicManager.play("Resources/Audios/Music/Electricity.wav", 0.6f, true);

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
        
        darknessManager = new DarknessManager(180.0f);
    
        winScreen = new WinScreen(this);
        loseScreen = new LoseScreen(this);
        pauseMenu = new PauseMenu(this);
        // ESC may still be held from a previous menu, so it has to be released before it can pause
        keyLocker.lockKey(Key.ESC);

        transitionPhase = TransitionPhase.NONE;
        fadeAlpha = 0;
        pendingMap = null;
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
                updateGhostArrivals();
                if (transitionPhase != TransitionPhase.NONE) {
                    updateTransition();
                    break;
                }
                player.update();
                map.update(player);

                darknessManager.update();
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
        updateGhostChaseMusic();
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
        // puzzles use the mouse, so only hide the cursor if one isn't open
        if (!map.getPuzzleUI().isOpen()) {
            Mouse.hideCursor();
        }
    }

    private void updateGhostChaseMusic() {
        boolean ghostNearby = false;
        if (player != null && map != null) {
            for (NPC npc : map.getNPCs()) {
                if (npc instanceof Ghost && ((Ghost) npc).isWithinDetectionRadius(player)) {
                    ghostNearby = true;
                    break;
                }
            }
        }

        String currentTrack = MusicManager.getCurrentFile();
        if (ghostNearby) {
            if (!ghostChaseMusicPlaying) {
                musicBeforeGhostChase = currentTrack;
                ghostChaseMusicPlaying = true;
            } else if (!GHOST_CHASE_MUSIC.equals(currentTrack)) {
                musicBeforeGhostChase = currentTrack;
            }

            if (!GHOST_CHASE_MUSIC.equals(currentTrack)) {
                MusicManager.play(GHOST_CHASE_MUSIC, 0.6f, true);
            }
        } else if (ghostChaseMusicPlaying) {
            if (GHOST_CHASE_MUSIC.equals(currentTrack)) {
                if (musicBeforeGhostChase == null) {
                    MusicManager.stop();
                } else {
                    MusicManager.play(musicBeforeGhostChase, 0.6f, true);
                }
            }
            musicBeforeGhostChase = null;
            ghostChaseMusicPlaying = false;
        }
    }

    @Override
    public void onWin() {
        // when this method is called within the game, it signals the game has been "won"
        playLevelScreenState = PlayLevelScreenState.LEVEL_COMPLETED;
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

    // player is frozen during the transition; the map keeps updating so the door script can finish and the camera can settle
    private void updateTransition() {
        if (transitionPhase == TransitionPhase.FADING_OUT) {
            map.update(player);
            fadeAlpha = Math.min(255, fadeAlpha + FADE_SPEED);
            if (fadeAlpha == 255) {
                swapToPendingMap();
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

            arrival.ghost.setLocation(safeX, safeY);

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
                winScreen.draw(graphicsHandler);
                break;
            case LOSE:
                loseScreen.draw(graphicsHandler);
                break;
        }
    }

    private void drawGame(GraphicsHandler graphicsHandler) {
        map.drawWorld(player, graphicsHandler);
        
        if (!map.isBookOpen()) {
            darknessManager.setTargetRadius(230);//180
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

        map.drawUI(graphicsHandler);

        // Display the XP bar above the game world.
        levelProgressHUD.draw(graphicsHandler);

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
