package HES;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
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

        //Envoi au Server de la chanson voulue
        sendSomethingToSomeone(exchangeSocket, musicChoice());

        //Recuperation depuis le server
        List<Object> clientMusicInfo =  readIncomingMessage2(exchangeSocket);

        InetAddress ipOfOtherClient = (InetAddress) clientMusicInfo.get(0);
        int port = (Integer) clientMusicInfo.get(1);

        System.out.println(ipOfOtherClient);
        System.out.println(port);

        //Thread.sleep(10000);

        Socket comSocket = null;

        //System.out.println("IP " + ipClient + " port " + port);
        try {
            comSocket = new Socket(ipOfOtherClient, port);
            ClientLogger.info("Client connected to other Client : socket" + ipOfOtherClient);
        } catch (IOException e) {
            ClientLogger.severe("IOException while connection to Other Client" + e.toString());
            e.printStackTrace();
        }


        sendSomethingToSomeone(comSocket,clientMusicInfo);
        Thread.sleep(10000);
        connectToOtherClient(ipOfOtherClient, port);


    }


}
