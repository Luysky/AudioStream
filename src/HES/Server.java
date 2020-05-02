package HES;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class Server {


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

    private Socket exchangeSocket = null;
    private InetAddress localAddress = null;
    private ServerSocket listeningSocket = null;
    private String interfaceName = "wlan1"; // ?? à determiner
    int ClientNumber = 1; // pour le premier client

    public Server() {

        startSocketServer();
        //sendInfoFromClient();

        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();


    }

    public void sendInfoFromClient() {

        /**
         * @Thomas
         * Methode integre la recuperation de la fusee d'information du client
         * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
         * de tous les clients
         */

        System.out.println("Client is connected");


        InputStream inputStream = null;
        try {
            inputStream = exchangeSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create a DataInputStream so we can read data from it.

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Recuperation des informations de la fusee venant du client

        List<Object> incomingRocket = null;
        try {
            incomingRocket = (List<Object>) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        List<Object> recupInfos = incomingRocket;
        idClient++;

        List<Object> clientPackage = new ArrayList<>();

        clientPackage.add(idClient);
        clientPackage.add(recupInfos);

        clientsList.add(clientPackage);


        // La suite est purement a des fins de test
        System.out.println();
        System.out.println("Receiving from client :");

        System.out.println("Client Name : " + recupInfos.get(0));
        System.out.println("Client Ip : " + recupInfos.get(1));
        //System.out.println("Client Port : " + recupInfos.get(2)); // il n'ya plus des port, j'ai changé le numero du tableau

        List<String> clientMusicList;
        clientMusicList = (List<String>) recupInfos.get(2);
        System.out.println("Chanson numero 4 : " + clientMusicList.get(2));

        List<Integer> clientMusicTime;
        clientMusicTime = (List<Integer>) recupInfos.get(3);
        System.out.println("Durée de la chanson numero 4 : " + clientMusicTime.get(2));

        String input = clientMusicList.get(2);
        input = input.substring(input.indexOf("'\'") + 1, input.lastIndexOf("."));

        System.out.println(input);


    }

    public void startSocketServer() {

        /**
         * @Thomas_et_Marina
         * Methode servant a initier les Sockets de Server et d'echange chez le Server,
         * le Serveur reste toujours en écoute et accept tous les clients qui se connectent.
         */

        System.out.println("Server starts");

        try {
            NetworkInterface ni = null;
            try {
                ni = NetworkInterface.getByName(interfaceName);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();

                if (!(ia instanceof Inet6Address) && !(ia.isLoopbackAddress())) {
                    if (!ia.isLoopbackAddress()) {
                        System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
                        localAddress = ia;
                    }
                }
            }

            try {

                listeningSocket = new ServerSocket(17257, 10, localAddress);

                System.out.println("Default Timeout :" + listeningSocket.getSoTimeout());
                System.out.println("Used IpAddress :" + listeningSocket.getInetAddress());
                System.out.println("Listening to Port :" + listeningSocket.getLocalPort());

                while (true) {
                    exchangeSocket = listeningSocket.accept();
                    System.out.println("I am listening ");
                    Thread acceptClientThread = new Thread(new AcceptClient(exchangeSocket, ClientNumber));
                    ClientNumber++;
                    acceptClientThread.start();
                    sendInfoFromClient();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}