package HES;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayer {

    /**
     * Marina
     * Classe servant a lire un clip audio reçu par un autre client en Inputstream
     */

    public Clip clip;
    public String status;
    public volatile AudioInputStream audioInputStream;


    public AudioPlayer(InputStream is) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        audioInputStream = AudioSystem.getAudioInputStream(is);

        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public synchronized void play(){
        clip.start();
        status = "play";
    }
}


