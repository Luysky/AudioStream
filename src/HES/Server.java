package HES;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server implements Runnable {


    //Chemin d'accès files audio Marina
    //private String path = "C://temp//AudioStream//myMusic//audio.wav";

    //Chemin d'accès files audio Thomas
    private String path = "C://temp//AudioStream//myMusic//David Bowie-Helden.wav";

    //Chemin d'accès log Thomas
    //private String myLog = "c://temp//AudioStream//my.log";

    //Chemin d'accès log Marina
    //private String myLog = "C://toSend//my.log";


    private List<Object> clientsList = new ArrayList<>();
    private int idClient = 1000;

    public Server(){

        startSocketServer();

        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();


    }



    //deprecated
    @Override
    public void run() {

        //path="C://toSend//audio.wav";
        // ajouter txt fichier pour ecrire le path et la taille du fichier wav

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
            //System.out.println("The file's size " + mybytearray.length);

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

    public void startSocketServer(){

        /**
         * @Thomas
         * Methode servant a initier les Sockets de Server et d'echange chez le Server
         * Pour l'instant elle integre egalement la recuperation de la fusee d'information du client
         * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
         * de tous les clients
         */

        System.out.println("Server online");

        try {

            ServerSocket listeningSocket = new ServerSocket(17257);
            Socket exchangeSocket = listeningSocket.accept();
            System.out.println("I am listening ");
            System.out.println("Client is connected");


            InputStream inputStream = exchangeSocket.getInputStream();

            // create a DataInputStream so we can read data from it.

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);



            // Recuperation des informations de la fusee venant du client

            List<Object> incomingRocket = (List<Object>) objectInputStream.readObject();


            List<Object> recupInfos = incomingRocket;
            idClient++;

            List<Object> clientPackage = new ArrayList<>();

            clientPackage.add(idClient);
            clientPackage.add(recupInfos);

            clientsList.add(clientPackage);


            // La suite est purement a des fins de test
            System.out.println();
            System.out.println("Receiving from client :");

            System.out.println("Client Name : "+recupInfos.get(0));
            System.out.println("Client Ip : "+recupInfos.get(1));
            System.out.println("Client Port : "+recupInfos.get(2));

            List<String>clientMusicList;
            clientMusicList = (List<String>) recupInfos.get(3);
            System.out.println("Chanson numero 4 : "+clientMusicList.get(3));

            List<Integer>clientMusicTime;
            clientMusicTime = (List<Integer>) recupInfos.get(4);
            System.out.println("Durée de la chanson numero 4 : "+clientMusicTime.get(3));

            String input = clientMusicList.get(3);
            input = input.substring(input.indexOf("'\'")+1, input.lastIndexOf("."));

            System.out.println(input);


            System.out.println("Closing sockets.");

            exchangeSocket.close();


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


}
