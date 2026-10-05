package Level;

import Utils.Direction;
import Utils.Point;

public interface GameListener {
    void onWin();

    // called when a script (such as a door trigger) wants the game to switch to a different map
    // spawnPosition is the pixel location the player should be placed at in the new map
    default void onChangeMap(Map newMap, Point spawnPosition, Direction facingDirection) { }
}
