package Screens;

import Engine.GraphicsHandler;
import Engine.MusicManager;
import Engine.SoundPlayer;
import java.awt.Color;

// menu shown over the game when the player presses ESC
// lets the player resume, change the music and sound effect volume, go back to the main menu, or quit
public class PauseMenu {
    private static final int RESUME = 0;
    private static final int MUSIC_VOLUME = 1;
    private static final int SOUND_VOLUME = 2;
    private static final int MAIN_MENU = 3;
    private static final int QUIT = 4;

    private static final float VOLUME_STEP = 0.1f;

    private final PlayLevelScreen playLevelScreen;
    private final OverlayMenu menu = new OverlayMenu("Paused", Color.WHITE,
            "Resume", "", "", "Main Menu", "Quit Game");

    public PauseMenu(PlayLevelScreen playLevelScreen) {
        this.playLevelScreen = playLevelScreen;
        menu.setHint("ESC: resume    LEFT/RIGHT or click: change volume");
    }

    public void open() {
        menu.open();
        updateVolumeLabels();
    }

    public void update() {
        int chosen = menu.update();

        // LEFT/RIGHT changes whichever volume is selected
        int adjust = menu.getAdjustDirection();
        if (adjust != 0 && menu.getSelected() == MUSIC_VOLUME) {
            MusicManager.setMasterVolume(stepVolume(MusicManager.getMasterVolume(), adjust));
        }
        else if (adjust != 0 && menu.getSelected() == SOUND_VOLUME) {
            SoundPlayer.setVolume(stepVolume(SoundPlayer.getVolume(), adjust));
        }

        switch (chosen) {
            case RESUME:
                playLevelScreen.resumeGame();
                break;
            // clicking a volume turns it up, wrapping back around to 0% after 100%
            case MUSIC_VOLUME:
                MusicManager.setMasterVolume(cycleVolume(MusicManager.getMasterVolume()));
                break;
            case SOUND_VOLUME:
                SoundPlayer.setVolume(cycleVolume(SoundPlayer.getVolume()));
                break;
            case MAIN_MENU:
                playLevelScreen.goBackToMenu();
                break;
            case QUIT:
                System.exit(0);
                break;
        }
        updateVolumeLabels();
    }

    private float stepVolume(float volume, int direction) {
        float stepped = Math.round((volume + direction * VOLUME_STEP) * 10) / 10f;
        return Math.max(0f, Math.min(1f, stepped));
    }

    private float cycleVolume(float volume) {
        return volume >= 1f ? 0f : stepVolume(volume, 1);
    }

    private void updateVolumeLabels() {
        menu.setOption(MUSIC_VOLUME, "Music Volume   < " + Math.round(MusicManager.getMasterVolume() * 100) + "% >");
        menu.setOption(SOUND_VOLUME, "Sound Volume   < " + Math.round(SoundPlayer.getVolume() * 100) + "% >");
    }

    public void draw(GraphicsHandler graphicsHandler) {
        menu.draw(graphicsHandler);
    }
}
