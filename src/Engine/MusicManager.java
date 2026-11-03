package Engine;


//To use, call MusicManager.play("path/to/music.wav", volume, loop) to start playing a track.
//To stop the music, call MusicManager.stop() or play the next track, which will automatically stop the previous track.

// The music files are big WAVs (40-75 MB) that are read fully into memory, which takes long enough to freeze the game,
// so new tracks load on a background thread and start when they're ready,
// and the last track switched away from stays loaded (paused) so switching back to it, like after a ghost chase, is instant
public class MusicManager {
    private static MusicPlayer current;
    private static String currentFile;
    private static float currentTrackVolume = 1f;

    // the last track switched away from, paused and ready to resume
    private static MusicPlayer previous;
    private static String previousFile;

    // counts play requests, so a track that finishes loading after a newer request doesn't start playing
    private static int requestId = 0;

    // player-chosen music volume from the pause menu (0.0 to 1.0), applied on top of each track's own volume
    private static float masterVolume = 1f;

    public static synchronized void play(String file, float volume, boolean loop) {
        if (file.equals(currentFile)) {
            // still loading, or already running? Leave it alone.
            if (current == null || current.isPlaying()) return;
            // a non-looping track that finished, start it again
            current.play();
            return;
        }

        requestId++;
        MusicPlayer resumed = file.equals(previousFile) ? previous : null;
        if (resumed != null) {
            previous = null;
            previousFile = null;
        }
        stashCurrent();
        currentFile = file;
        currentTrackVolume = volume;

        if (resumed != null) {
            // switching back to the track that was just playing: pick it up where it left off
            current = resumed;
            current.setLooping(loop);
            current.setVolume(volume * masterVolume);
            if (current.getState() == MusicPlayer.State.PAUSED) {
                current.resume();
            } else {
                current.play();
            }
            return;
        }

        int request = requestId;
        Thread loader = new Thread(() -> {
            MusicPlayer player = new MusicPlayer();
            player.setMusicFile(file);
            player.setLooping(loop);
            synchronized (MusicManager.class) {
                if (request != requestId) {
                    // something else was asked for while this loaded
                    player.dispose();
                    return;
                }
                current = player;
                current.setVolume(currentTrackVolume * masterVolume);
                current.play();
            }
        }, "music-loader");
        loader.setDaemon(true);
        loader.start();
    }

    // pauses the current track and keeps it as the previous one, freeing whatever was there before
    private static void stashCurrent() {
        if (previous != null) {
            previous.dispose();
        }
        previous = current;
        previousFile = current == null ? null : currentFile;
        if (previous != null) {
            previous.pause();
        }
        current = null;
        currentFile = null;
    }

    public static synchronized float getMasterVolume() {
        return masterVolume;
    }

    public static synchronized void setMasterVolume(float volume) {
        masterVolume = Math.max(0f, Math.min(1f, volume));
        if (current != null) {
            current.setVolume(currentTrackVolume * masterVolume);
        }
    }

    public static synchronized void stop() {
        requestId++; // a track still loading shouldn't start after this
        if (current != null) {
            current.dispose();   // stops AND closes the clip
            current = null;
        }
        currentFile = null;
    }

    public static synchronized String getCurrentFile() {
        return currentFile;
    }
}
