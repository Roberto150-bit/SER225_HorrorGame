package Engine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.util.Random;

// plays short sound effects from Resources/Audios
public class SoundPlayer {
    public static final String AUDIO_PATH = Config.RESOURCES_PATH + "Audios/";

    private static final Random random = new Random();

    // player-chosen sound effect volume from the pause menu (0.0 to 1.0)
    private static float volume = 1f;

    public static float getVolume() {
        return volume;
    }

    public static void setVolume(float newVolume) {
        volume = Math.max(0f, Math.min(1f, newVolume));
    }

    // plays a sound once
    public static void play(String soundFileName) {
        if (volume <= 0f) {
            return;
        }
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(AUDIO_PATH + soundFileName));
            audioStream = convertTo16Bit(audioStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            applyVolume(clip);
            // close the clip when it's done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("Unable to play sound " + AUDIO_PATH + soundFileName + ": " + e.getMessage());
        }
    }

    // plays one of the sounds at random
    public static void playRandom(String... soundFileNames) {
        play(soundFileNames[random.nextInt(soundFileNames.length)]);
    }

    // converts the 0..1 volume into decibels for the clip
    private static void applyVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (20.0 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }

    // Java can't play 24 bit audio, so it's converted to 16 bit
    private static AudioInputStream convertTo16Bit(AudioInputStream audioStream) {
        AudioFormat format = audioStream.getFormat();
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleSizeInBits() == 16) {
            return audioStream;
        }
        AudioFormat format16Bit = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false);
        return AudioSystem.getAudioInputStream(format16Bit, audioStream);
    }

    // prevents SoundPlayer from being instantiated
    private SoundPlayer() { }
}
