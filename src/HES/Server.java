package HES;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private final static Logger ServerLogger = Logger.getLogger("ServerLog");

    //Chemin d'accès log Thomas
    //private String myLog = "c://temp//AudioStream//my.log";

    //Chemin d'accès log Marina
    //private String myLog = "C://toSend//my.log";

    private List<Object> clientsList = new ArrayList<>();
    private int idClient = 1;

    private ServerSocket serverListeningSocket = null;
    private Socket serverExchangeSocket = null;

    private InetAddress localAddress = null;
    private String interfaceName = "wlan1"; // ?? à determiner
    private int ClientNumber = 1; // pour le premier client

    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = dateFormat.format(currentDate.getTime());

    private InputStream inputStream = null;
    private ObjectInputStream objectInputStream = null;



    public Server() {

        startServerLogger();
        //sendInfoFromClient();

        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();


    }

    public void startServerLogger(){


        FileHandler fh = null;
        try {
            fh = new FileHandler("C://temp//AudioStream//Server" + dateNow + ".log", false);
        } catch (IOException e) {
            e.printStackTrace();
            ServerLogger.severe("IOException of FileHandler " + e.toString());
        }
        CustomFormatter customFormatter = new CustomFormatter();
        fh.setFormatter(customFormatter);

        ServerLogger.addHandler(fh);
        ServerLogger.setLevel(Level.INFO);
        ServerLogger.info("******************** Program starts ********************");

        findConnectionInfo();
        startServerSocket();


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
            System.out.println("Connection Timed out");
            ServerLogger.severe("Connection Timed out");
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

    public void startServerSocket() {

        /**
         * @Thomas_et_Marina
         * Methode servant a initier les Sockets de Server et d'echange chez le Server,
         * le Serveur reste toujours en écoute et accept tous les clients qui se connectent.
         */


        try {

            findConnectionInfo();


            try {

                serverListeningSocket = new ServerSocket(17257, 10, localAddress);
                ServerLogger.info("Default Timeout :" + serverListeningSocket.getSoTimeout());
                ServerLogger.info("Listening to Port :" + serverListeningSocket.getLocalPort());
                System.out.println();

                while (true) {
                    serverExchangeSocket = serverListeningSocket.accept();

                    System.out.println("******************************************");

                    System.out.println("I am listening ");
                    Thread acceptClientThread = new Thread(new AcceptClient(serverExchangeSocket, ClientNumber));
                    ClientNumber++;
                    acceptClientThread.start();
                    receiveInfoFromClient();

                    //retreivedIpFromClient(clientsList,0);

                    //System.out.println(musicString(giveMusicList(0)));

                    sendMusicMenu();
                    System.out.println("Test j'ai passé sendMusic");

                }

            } catch (IOException e) {
                e.printStackTrace();
                ServerLogger.severe("IO exception " + e.toString());
            }


        } catch (Exception e) {
            e.printStackTrace();
            ServerLogger.severe("Exception " + e.toString());
        }
    }

    public void receiveInfoFromClient() {

        /**
         * @Thomas
         * Methode integre la recuperation de la fusee d'information du client (Regroupement d'infor de base du client)
         * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
         * de tous les clients
         */

        //System.out.println("A client is connected");
        ServerLogger.info("A client is connected");


        InputStream inputStream = null;
        try {
            inputStream = serverExchangeSocket.getInputStream();
        } catch (IOException e) {
            ServerLogger.severe("IO Exception in InputStream " + e.toString());
        }

        // create a DataInputStream so we can read data from it.

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            ServerLogger.severe("IO Exception in ObjectInputStream " + e.toString());
        }


        // Recuperation des informations de la fusee venant du client

        List<Object> incomingRocket = null;
        try {
            incomingRocket = (List<Object>) objectInputStream.readObject();
        } catch (IOException e) {
            ServerLogger.severe("IOException " + e.toString());
        } catch (ClassNotFoundException e) {
            ServerLogger.severe("ClassNotFoundException " + e.toString());
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
        ServerLogger.info("Client IP :" + recupInfos.get(1));
        System.out.println("Client Port : " + recupInfos.get(2)); // il n'ya plus des port, j'ai changé le numero du tableau
        System.out.println("Client music :" + recupInfos.get(3));
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

    public void writeToClient (InetAddress clientIp, int clientPort,List<String> musicList) throws IOException {

        InputStream iS = serverExchangeSocket.getInputStream();
        InputStreamReader iSr = new InputStreamReader(iS);
        BufferedReader buffin = new BufferedReader(iSr);

        PrintWriter pout = new PrintWriter(serverExchangeSocket.getOutputStream());



        String messageToSend = "Le server te salue !";
        pout.println(messageToSend);

    }



    public void sendMusicMenu() throws IOException {

        for(int i = 0; i<clientsList.size(); i++) {

            InetAddress clientIp = retreivedIpFromClient(clientsList, i);
            int port = retreivedPortFromClient(clientsList, i);

            String message = musicString(giveMusicList(i));

            if(message!="") {
                sendSomethingToClient(clientIp, port, message);
            }
            else{
                String sorry = "Aucun audio actuellement disponbile";
                sendSomethingToClient(clientIp,port,sorry);
            }
        }


    }

    public List<String> giveMusicList(int clientNumber){

        /**
         * @author Thomas
         * Methode qui va recuperer la liste des audios d'un client.
         */

        List<Object>tempList = new ArrayList<>();
        List<Object>packageList = new ArrayList<>();
        List<String>musicList = new ArrayList<>();

        for(int i = 0; i<clientsList.size();i++){

            if(clientNumber==i){
                System.out.println("Je passe par continue");
                continue;
            }

            else {
                //On ouvre le bon clientList
                tempList = (List<Object>) clientsList.get(i);
                //clientListNr++;

                //On recupere la liste d'infos du client
                packageList = (List<Object>) tempList.get(1);

                //On selectionne la liste de music et on l'ajoute au menu des music
                musicList = (List<String>) packageList.get(3);
            }
        }

        return musicList;

    }

    public String musicString (List<String> music){


        String message = "";
        String messageTemp = "";

        int cpt = 0;


        Iterator<String> j = music.iterator();

        while (j.hasNext()) {

            messageTemp = j.next();
            messageTemp = messageTemp.substring(messageTemp.indexOf("myMusic") + 8, messageTemp.lastIndexOf("."));
            cpt++;

            message += (cpt+". "+ messageTemp+"\n");
        }


        return message;

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








}