package Engine;


//To use, call MusicManager.play("path/to/music.wav", volume, loop) to start playing a track. 
//To stop the music, call MusicManager.stop() or play the next track, which will automatically stop the previous track.

public class MusicManager {
    private static MusicPlayer current;
    private static String currentFile;
    private static float currentTrackVolume = 1f;

    // player-chosen music volume from the pause menu (0.0 to 1.0), applied on top of each track's own volume
    private static float masterVolume = 1f;

    public static void play(String file, float volume, boolean loop) {
        // Same track already running? Leave it alone.
        if (file.equals(currentFile) && current != null && current.isPlaying()) return;

        stop(); // frees the previous track

        current = new MusicPlayer();
        current.setMusicFile(file);
        current.setLooping(loop);
        current.setVolume(volume * masterVolume);
        current.play();
        currentFile = file;
        currentTrackVolume = volume;
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0f, Math.min(1f, volume));
        if (current != null) {
            current.setVolume(currentTrackVolume * masterVolume);
        }
    }

    public static void stop() {
        if (current != null) {
            current.dispose();   // stops AND closes the clip
            current = null;
            currentFile = null;
        }
    }

    public static String getCurrentFile() {
        return currentFile;
    }
}
