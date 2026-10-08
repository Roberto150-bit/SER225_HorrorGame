package Game;

import Engine.GameWindow;
import Engine.ScreenManager;

/*
 * The game starts here
 * This class just starts up a GameWindow and attaches the ScreenCoordinator to the ScreenManager instance in the GameWindow
 * From this point on the ScreenCoordinator class will dictate what the game does
 */
public class Game {

    public static void main(String[] args) {
        new Game();
    }

    public Game() {
        GameWindow gameWindow = new GameWindow();
        ScreenManager screenManager = gameWindow.getScreenManager();
        screenManager.setCurrentScreen(new ScreenCoordinator());
        gameWindow.startGame();
    }
}
//

// I will excercise at the gym twice a week
// I will not buy candy as a snack for the next 2 weeks