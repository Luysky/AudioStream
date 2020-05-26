package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ClientToClient implements Runnable {

    final BufferedInputStream bis;
    Socket s;


    public ClientToClient(Socket s, BufferedInputStream bis){
        this.s = s;
        this.bis=bis;
    }

    @Override
    public void run() {

        System.out.println("Client enters run() ");

        try {
            System.out.println("Try to put bis into Player");
            AudioPlayer player = new AudioPlayer(bis);
            System.out.println("Try  to play");
            player.play();
            System.out.println("I am playing");
            try {
                Thread.sleep(player.clip.getMicrosecondLength());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }


    }
}
