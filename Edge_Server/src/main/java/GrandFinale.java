import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GrandFinale implements Runnable {

    @Override
    public void run() {

        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File
                    ("./music/finale.wav")
            );
            clip.open(inputStream);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }
}
