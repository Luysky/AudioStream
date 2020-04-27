package HES;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server implements Runnable {


    //Chemin d'accès files audio Marina
    //private String path = "C://temp//AudioStream//myMusic//audio.wav";

    //Chemin d'accès files audio Thomas
    private String path = "C://temp//AudioStream//myMusic//David Bowie-Helden.wav";

    //Chemin d'accès log Thomas
    //private String myLog = "c://temp//AudioStream//my.log";

    //Chemin d'accès log Marina
    //private String myLog = "C://toSend//my.log";




    public Server(){


        run();


    }



    @Override
    public void run() {

        //path="C://toSend//audio.wav"; //  ajouter txt fichier pour ecrire le path et la taille du fichier wav

        //ServerLog sl = new ServerLog();


        System.out.println("Server online");

        try {

            ServerSocket listeningSocket = new ServerSocket(17257);
            Socket exchangeSocket = listeningSocket.accept();
            System.out.println("I am listening ");
            System.out.println("Client is connected");

            // ClientOne communique l'adresse et le nom de son fichier audio
            File myFile = new File(path);
            byte[] mybytearray = new byte[(int)myFile.length()];
            System.out.println("The file's size " + mybytearray.length);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(mybytearray, 0, mybytearray.length);

            // Le Server communique l'info du clientOne

            OutputStream os = exchangeSocket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            exchangeSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
