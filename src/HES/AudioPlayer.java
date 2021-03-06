package HES;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Marina
 * Classe servant a lire un clip audio reçu par un autre client en Inputstream
 */

public class AudioPlayer {


    public Clip clip;
    public String status;
    public AudioInputStream audioInputStream;


    public AudioPlayer(InputStream is) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        audioInputStream = AudioSystem.getAudioInputStream(is);

        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void play(){
        clip.start();
        status = "play";
    }
}


