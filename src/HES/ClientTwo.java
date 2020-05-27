package HES;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientTwo extends ClientAlpha {

    public ClientTwo() throws IOException, InterruptedException {

        //clientName="Marshasha";
        myMusicRepertory="C://temp//AudioStream2//myMusic";

        portClientClient = 25250;



      //  ClientTwo clientTwo = new ClientTwo();

        start();

        List<Object>infoSong = null;

        infoSong = listenTo();

        String chanson = (String) infoSong.get(2);
        System.out.println("Song : "+chanson);

        listeningToClients(infoSong);


    }
}
