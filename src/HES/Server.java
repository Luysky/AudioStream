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

    private String path;

    @Override
    public void run() {

        path="C://toSend//audio.wav"; //  ajouter txt fichier pour ecrire le path et la taille du fichier wav

        Logger log = Logger.getLogger("myLogger");
        try {
            FileHandler f = new FileHandler("C://toSend//my.log", true);
            // SimpleFormatter formatter = new SimpleFormatter();
            CustomFormatter formatter = new CustomFormatter();

            log.addHandler(f);
            f.setFormatter(formatter);
            log.setLevel(Level.INFO);
            log.info("\n-------------this is the info level------");
            log.warning("attention hacker");
            log.severe("exception");

            log.setLevel(Level.WARNING);
            log.warning("\n-------------this is the warning level------");
            log.warning("attention hacker");
            log.severe("exception");

            log.setLevel(Level.SEVERE);
            log.severe("\n-------------this is severe level------");
            log.warning("attention hacker");
            log.severe("exception");

        } catch (IOException e) {
            e.printStackTrace();
        }



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
