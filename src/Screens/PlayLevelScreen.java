package Screens;

import Engine.GraphicsHandler;
import Engine.Screen;
import Engine.ScreenManager;
import Game.GameState;
import Game.ScreenCoordinator;
import Level.*;
import Maps.BasementMap;
import Players.Cat;
import Players.DeanPlayer;
import Utils.Direction;
import Utils.Point;

import java.awt.Color;
import Lighting.DarknessManager;

// This class is for when the RPG game is actually being played
public class PlayLevelScreen extends Screen implements GameListener {
    protected ScreenCoordinator screenCoordinator;
    protected Map map;
    protected Player player;
    protected PlayLevelScreenState playLevelScreenState;
    protected WinScreen winScreen;
    protected FlagManager flagManager;

    // map transition (door) state -- the screen fades to black, swaps maps, then fades back in
    protected static final int FADE_SPEED = 15; // alpha change per frame (0-255)
    protected TransitionPhase transitionPhase = TransitionPhase.NONE;
    protected int fadeAlpha = 0;
    protected Map pendingMap;
    protected Point pendingSpawnPosition;
    protected Direction pendingFacingDirection;
    private DarknessManager darknessManager;

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

        map = new BasementMap();

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
        
        darknessManager = new DarknessManager(90.0f);
    
        winScreen = new WinScreen(this);

        transitionPhase = TransitionPhase.NONE;
        fadeAlpha = 0;
        pendingMap = null;
    }

    public void update() {
        // based on screen state, perform specific actions
        switch (playLevelScreenState) {
            // if level is "running" update player and map to keep game logic for the platformer level going
            case RUNNING:
                if (transitionPhase != TransitionPhase.NONE) {
                    updateTransition();
                    break;
                }
                player.update();
                map.update(player);
                darknessManager.update();
                break;
            // if level has been completed, bring up level cleared screen
            case LEVEL_COMPLETED:
                winScreen.update();
                break;
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
        pendingMap = newMap;
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
        // carry shared state (flags, held items, listeners) over to the new map
        pendingMap.setFlagManager(flagManager);
        pendingMap.setHotbarUI(map.getHotbarUI());
        pendingMap.setPlayer(player);
        pendingMap.addListener(this);

        player.setMap(pendingMap);
        player.setLocation(pendingSpawnPosition.x, pendingSpawnPosition.y);
        player.setFacingDirection(pendingFacingDirection);
        player.setPlayerState(PlayerState.STANDING);

        pendingMap.preloadScripts();
        map = pendingMap;
        pendingMap = null;

        // snaps the camera to the player's new position before the first frame is drawn
        map.update(player);
    }

    public void draw(GraphicsHandler graphicsHandler) {
        // based on screen state, draw appropriate graphics
        switch (playLevelScreenState) {
            case RUNNING:
                map.draw(player, graphicsHandler);
                break;
            case LEVEL_COMPLETED:
                winScreen.draw(graphicsHandler);
                break;
        }
    }

    public PlayLevelScreenState getPlayLevelScreenState() {
        return playLevelScreenState;
    }

    public void resetLevel() {
        initialize();
    }

    public void goBackToMenu() {
        screenCoordinator.setGameState(GameState.MENU);
    }

    // This enum represents the different states this screen can be in
    private enum PlayLevelScreenState {
        RUNNING, LEVEL_COMPLETED
    }

    private enum TransitionPhase {
        NONE, FADING_OUT, FADING_IN
    }
}
