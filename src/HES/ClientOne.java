package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class ClientOne extends ClientAlpha{

    // ClientOne a le code du Server et du Client


    public ClientOne() throws IOException {

        clientName="Client one";
        portClient=22545;

        startClientSockets();


        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();


    }


}
