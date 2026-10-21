package Engine;
 
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
 
/**
Is run through music manager to play music in the game. 
Should only play one music track at a time, and should be used for background music.

To use, call MusicManager.play("path/to/music.wav", volume, loop) to start playing a track. 
To stop the music, call MusicManager.stop() or play the next track, which will automatically stop the previous track.
*/

public class MusicPlayer {
 
    public enum State { STOPPED, PLAYING, PAUSED } //States used to manipulate any instanace of music.
 
    private State state = State.STOPPED;
    private Clip clip;
    private float volume = 1.0f;     // 0.0 (silent) to 1.0 (full)
    private boolean looping = false;
 
    public synchronized void play() {//plays the music using the clip object
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        startPlayback();
        state = State.PLAYING;
    }
 
    public synchronized void stop() {
        if (clip == null) return;
        state = State.STOPPED;
        clip.stop();
        clip.setFramePosition(0);
    }
 
    public synchronized void pause() {
        refreshState();
        if (clip == null || state != State.PLAYING) return;
        clip.stop(); // keeps the current frame position
        state = State.PAUSED;
    }
 
    public synchronized void resume() {
        if (clip == null || state != State.PAUSED) return;
        startPlayback();
        state = State.PLAYING;
    }
 
    /** @param volume 0.0 (silent) to 1.0 (full volume) */
    public synchronized void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        applyVolume();
    }
 
    public synchronized void setLooping(boolean looping) {
        this.looping = looping;
        refreshState();
        if (clip != null && state == State.PLAYING) {
            clip.loop(looping ? Clip.LOOP_CONTINUOUSLY : 0);
        }
    }
 
    // Returns the current playback position in seconds 
    public synchronized float getTimeline() {
        if (clip == null) return 0.0f;
        return clip.getFramePosition() / clip.getFormat().getFrameRate();
    }
 
    // Sets the current playback position in seconds, can be used to skip forward or backward in the track
    public synchronized void setTimeline(float position) {
        if (clip == null) return;
        float max = getLength();
        float clamped = Math.max(0f, Math.min(position, max));
        int frame = (int) (clamped * clip.getFormat().getFrameRate());
        clip.setFramePosition(Math.min(frame, clip.getFrameLength() - 1));
    }
 
    public synchronized void dispose() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        state = State.STOPPED;
    }
 
    /** Loads a new track (replacing the current one). Does not start playing. */
    public synchronized void setMusicFile(String filePath) {
        dispose();
 
        File file = new File(filePath);
        try (AudioInputStream raw = AudioSystem.getAudioInputStream(file)) {
            AudioInputStream pcm = raw;
            AudioFormat base = raw.getFormat();
 
            // Convert anything that isn't signed PCM (e.g. compressed formats)
            if (base.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat decoded = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        base.getSampleRate(),
                        16,
                        base.getChannels(),
                        base.getChannels() * 2,
                        base.getSampleRate(),
                        false);
                pcm = AudioSystem.getAudioInputStream(decoded, raw);
            }
 
            Clip newClip = AudioSystem.getClip();
            newClip.open(pcm); // reads the whole file into memory
            clip = newClip;
            applyVolume();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("MusicPlayer: unsupported audio format (use WAV): " + filePath);
        } catch (IOException e) {
            System.err.println("MusicPlayer: could not read file: " + filePath);
        } catch (LineUnavailableException e) {
            System.err.println("MusicPlayer: no audio line available for: " + filePath);
        }
    }
 
    // ---- extras that are handy in a game ----
 
    public synchronized State getState() {
        refreshState();
        return state;
    }
 
    public synchronized boolean isPlaying() {
        return getState() == State.PLAYING;
    }
 
    /** @return total track length in seconds */
    public synchronized float getLength() {
        if (clip == null) return 0.0f;
        return clip.getFrameLength() / clip.getFormat().getFrameRate();
    }
 
    // ---- internals ----
 
    private void startPlayback() {
        if (looping) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
    }
 
    /** If a non-looping track finished on its own, mark it STOPPED and rewind. */
    private void refreshState() {
        if (clip != null && state == State.PLAYING && !clip.isRunning()) {
            state = State.STOPPED;
            clip.setFramePosition(0);
        }
    }
 
    /** Converts the 0..1 volume into decibels and applies it to the clip. */
    private void applyVolume() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (volume <= 0.0001f) ? gain.getMinimum() : (float) (20.0 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }
}
