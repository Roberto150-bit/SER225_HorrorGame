package Engine;


//To use, call MusicManager.play("path/to/music.wav", volume, loop) to start playing a track. 
//To stop the music, call MusicManager.stop() or play the next track, which will automatically stop the previous track.

public class MusicManager {
    private static MusicPlayer current;
    private static String currentFile;

    public static void play(String file, float volume, boolean loop) {
        // Same track already running? Leave it alone.
        if (file.equals(currentFile) && current != null && current.isPlaying()) return;

        stop(); // frees the previous track

        current = new MusicPlayer();
        current.setMusicFile(file);
        current.setLooping(loop);
        current.setVolume(volume);
        current.play();
        currentFile = file;
    }

    public static void stop() {
        if (current != null) {
            current.dispose();   // stops AND closes the clip
            current = null;
            currentFile = null;
        }
    }
}

