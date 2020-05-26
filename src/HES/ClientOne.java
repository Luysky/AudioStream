package HES;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

public class ClientOne extends ClientAlpha {


    public ClientOne() throws IOException, InterruptedException {

        //clientName="Luysky";
        myMusicRepertory="C://temp//AudioStream//myMusic";

        portClientClient = 25245;

        //InetAddress ipOfOtherClient = InetAddress.getByName("192.168.0.15");
        //int port = 25250;

        //String path="C://temp//AudioStream2//myMusic//audio.wav";
        //File myFile = new File(path);
        //byte[] mybytearray = new byte[(int)myFile.length()];

        start();
        sendSomethingToSomeone(exchangeSocket, musicChoice());
        //Thread.sleep(10000);



        List<Object> clientMusicInfo =  readIncomingMessage2(exchangeSocket);

        System.out.println("Je suis là");

        InetAddress ipOfOtherClient = (InetAddress) clientMusicInfo.get(0);
        int port = (Integer) clientMusicInfo.get(1);

        System.out.println(ipOfOtherClient);
        System.out.println(port);

        connectToOtherClient(ipOfOtherClient, port);


    }


}
