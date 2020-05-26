package HES;

import java.io.IOException;
import java.net.Socket;

public class ClientTwo extends ClientAlpha {

    public ClientTwo() throws IOException, InterruptedException {

        //clientName="Marshasha";
        myMusicRepertory="C://temp//AudioStream2//myMusic";

        portClientClient = 25250;



      //  ClientTwo clientTwo = new ClientTwo();

        start();
        listeningToClients();


    }
}
