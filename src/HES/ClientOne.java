package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientOne extends ClientAlpha{

    // ClientOne a le code du Server et du Client


    private byte [] mybytearray = new byte [6000000];


    public ClientOne(){


        run();

    }


}
