package HES;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AudioPlayer {

    //Class a decrire

    public long currentFrame;
    public Clip clip;
    public String status;
    public AudioInputStream audioInputStream;
    public static String filePath;


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

    public void pause(){

        if(status.equals("paused")){
            System.out.println("The file is already paused");
            return;
        }

        this.currentFrame = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

}


