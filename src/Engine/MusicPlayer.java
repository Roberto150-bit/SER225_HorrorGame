package Engine;

public class MusicPlayer {

    public enum State { STOPPED, PLAYING, PAUSED }
    private State state = State.STOPPED;

    public void play() {
        // Code to play music
    }

    public void stop() {
        // Code to stop music
    }

    public void pause() {
        // Code to pause music
    }

    public void resume() {
        // Code to resume music
    }  

    public void setVolume(float volume) {
        // Code to set the volume of the music
    }

    public void setLooping(boolean looping) {
        // Code to set whether the music should loop
    }

    public float getTimeline() {
        // Code to get the current position of the music playback
        return 0.0f;
    }

    public void setTimeline(float position) {
        // Code to set the position of the music playback
    }

    public void dispose() {
        // Code to release resources used by the music player
    }

    public void setMusicFile(String filePath) {
        // Code to set the music file to be played
    }
}
