package HES;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
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

    private Socket serverComClientSocket = null;
    private InetAddress localAddress = null;
    private ServerSocket serverListeningSocket = null;
    private String interfaceName = "wlan1"; // ?? à determiner
    private int ClientNumber = 1; // pour le premier client

    public Server() {

        startSocketServer();
        //sendInfoFromClient();

        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();


    }

    public void receiveInfoFromClient() {

        /**
         * @Thomas
         * Methode integre la recuperation de la fusee d'information du client (Regroupement d'infor de base du client)
         * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
         * de tous les clients
         */

        System.out.println("A client is connected");


        InputStream inputStream = null;
        try {
            inputStream = serverComClientSocket.getInputStream();
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
        System.out.println("Client Port : " + recupInfos.get(2)); // il n'ya plus des port, j'ai changé le numero du tableau

        System.out.println();

        //List<String> clientMusicList;
        //clientMusicList = (List<String>) recupInfos.get(2);
        //System.out.println("Chanson numero 1 : " + clientMusicList.get(1));

        //List<Integer> clientMusicTime;
        //clientMusicTime = (List<Integer>) recupInfos.get(3);
        //System.out.println("Durée de la chanson numero 1 : " + clientMusicTime.get(1));

        //String input = clientMusicList.get(1);
        //input = input.substring(input.indexOf("myMusic")+8, input.lastIndexOf("."));

        //System.out.println(input);
        //giveMusicList(1);

    }

    public void sendSomethingToClient(InetAddress clientIp, int clientPort,Object object) throws IOException {

        /**
         * @author Thomas
         * Methode qui va etre utilisee pour envoyer des informations/un objet a un client.
         */

        Socket clientSocket = null;
        try {
            clientSocket = new Socket(clientIp, clientPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = clientSocket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(object);


    }


    public InetAddress retreivedIpFromClient(List<Object>clientlist, int client){

        /**
         * @author Thomas
         * Methode qui va recuperer l'adresse Ip envoyee par un client
         */

        List<Object>tempList = (List<Object>) clientlist.get(client);
        List<Object>packageList = (List<Object>) tempList.get(1);
        InetAddress temp = (InetAddress) packageList.get(1);

        return temp;
    }

    public int retreivedPortFromClient(List<Object>clientsList, int client){

        /**
         * @author Thomas
         * Methode qui va recuperer l'adresse de port envoyee par un client
         */

        List<Object>tempList = (List<Object>) clientsList.get(client);
        List<Object>packageList = (List<Object>) tempList.get(1);
        int temp = (int) packageList.get(2);

        return temp;

    }

    public void giveMusicList(int clientNumber){

        /**
         * @author Thomas
         * Methode qui va recuperer la liste des audios d'un client.
         */

        List<Object>tempList = new ArrayList<>();
        List<Object>packageList = new ArrayList<>();
        List<String>musicList = new ArrayList<>();


        //A modifier clientNumber
        for(int i = 0; i<clientsList.size();i++){

            if(clientNumber==i){
                i++;
            }

            else {
                //On ouvre le bon clientList
                tempList = (List<Object>) clientsList.get(i);

                //On récupère la liste d'infos du client
                packageList = (List<Object>) tempList.get(1);

                //On sélectionne la liste de music et on l'ajoute au menu des music
                musicList = (List<String>) packageList.get(3);
            }

            listIterator(musicList);

        }


    }

    public void listIterator(List<String> c) {

        /**
         * @author Thomas
         * Methode permettant d'afficher une liste de String avec numerotation devant chaque element
         * et substring pour garder uniquement le nom de l'audio.
         * Ex : 1. Miyagi
         */

        int cpt = 1;

        Iterator<String> i = c.iterator();

        while (i.hasNext()) {

            String l = i.next();
            l = l.substring(l.indexOf("myMusic")+8, l.lastIndexOf("."));
            System.out.println(cpt + ". " + l);
            cpt++;
        }

        System.out.println();

    }


    public void startSocketServer() {

        /**
         * @Thomas_et_Marina
         * Methode servant a initier les Sockets de Server et d'echange chez le Server,
         * le Serveur reste toujours en écoute et accept tous les clients qui se connectent.
         */

        System.out.println("Server starts");

        try {

            findConnectionInfo();


            try {

                serverListeningSocket = new ServerSocket(17257, 10, localAddress);

                //System.out.println("Default Timeout :" + listeningSocket.getSoTimeout());
                //System.out.println("Used IpAddress :" + listeningSocket.getInetAddress());
                System.out.println("Listening to Port :" + serverListeningSocket.getLocalPort());
                System.out.println();

                while (true) {
                    serverComClientSocket = serverListeningSocket.accept();

                    System.out.println("******************************************");

                    System.out.println("I am listening ");
                    Thread acceptClientThread = new Thread(new AcceptClient(serverComClientSocket, ClientNumber));
                    ClientNumber++;
                    acceptClientThread.start();
                    receiveInfoFromClient();

                    retreivedIpFromClient(clientsList,0);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findConnectionInfo(){

        /**
         * @author Thomas
         * Methode qui va faire les recherches de l'interface et de l'adresse IP utilisé par le server.
         * De cette manière le code fonctionne sur n'importe quelle machine sans réglage supplémentaire.
         */

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

    }

}