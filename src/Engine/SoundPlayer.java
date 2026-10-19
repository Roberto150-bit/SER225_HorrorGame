package Engine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.File;
import java.util.Random;

// plays short sound effects from Resources/Audios
public class SoundPlayer {
    public static final String AUDIO_PATH = Config.RESOURCES_PATH + "Audios/";

    private static final Random random = new Random();

    // plays a sound once
    public static void play(String soundFileName) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(AUDIO_PATH + soundFileName));
            audioStream = convertTo16Bit(audioStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
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
