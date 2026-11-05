package Level;

import Utils.Direction;
import Utils.Point;

public interface GameListener {
    void onWin();

    // called when a script (such as a door trigger) wants the game to switch to a different map
    // spawnPosition is the pixel location the player should be placed at in the new map
    default void onChangeMap(Map newMap, Point spawnPosition, Direction facingDirection) { }

    // called when the player reaches a checkpoint event (like solving a puzzle for the first time)
    default void onCheckpointReached() { }

    // fades the screen to black, runs change while it's black (e.g. a room reshaping itself), then fades back in
    default void onFadeAndChange(Runnable change) { }

    // shakes the screen for the given number of frames (rumbling stone, a door rising)
    default void onScreenShake(int frames) { }
}
