package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientOne implements Runnable{

    // ClientOne a le code du Server et du Client

    private String ipAddress = "192.168.0.15";
    private int port = 17257;

    private String path = "C://toGet//audio.wav";
    private byte [] mybytearray = new byte [6000000];

    @Override
    public void run() {

        System.out.println("Client starts");


    try{

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Socket exchangeSocket = new Socket(ipAddress, port);
        System.out.println("I am connected");

        InputStream is = new BufferedInputStream(exchangeSocket.getInputStream());

        try {
            AudioPlayer player = new AudioPlayer(is);
            player.play();
            try {
                Thread.sleep(player.clip.getMicrosecondLength());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

            /*FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = is.read(mybytearray, 0, mybytearray.length);
            bos.write(mybytearray, 0, mybytearray.length);

            bos.close(); */

        is.close();
        exchangeSocket.close();


    }catch(IOException e){
        e.printStackTrace();
    }
}
}
