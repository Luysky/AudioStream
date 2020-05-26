package HES;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientOne extends ClientAlpha {


    public ClientOne() throws IOException, InterruptedException {

        //clientName="Luysky";
        myMusicRepertory="C://temp//AudioStream//myMusic";

        portClientClient = 25245;

        InetAddress ipOfOtherClient = InetAddress.getByName("192.168.0.15");
        int port = 25250;

        String path="C://temp//AudioStream2//myMusic//audio.wav";
        File myFile = new File(path);
        byte[] mybytearray = new byte[(int)myFile.length()];

        start();

        Thread.sleep(10000);
        connectToOtherClient(ipOfOtherClient, port);


    }


}
