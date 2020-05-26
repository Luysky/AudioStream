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

    private List<Object> clientsList = new ArrayList<>();
    private int idClient = 0;

    private ServerSocket serverListeningSocket = null;
    protected Socket [] serverExchangeSocket = new Socket [2];

    private InetAddress localAddress = null;
    private String interfaceName = "wlan1"; // ?? à determiner
    private int ClientNumber = 1; // pour le premier client

    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = dateFormat.format(currentDate.getTime());

    private InputStream inputStream = null;
    private ObjectInputStream objectInputStream = null;

    private String clientActive = null;
    private String clientAnswer = null;


    public Server() {

        startServerLogger();
        serverListeningSocket = startServerSocket();

        boolean useOfMethode = true;
        int cptloop = 0;

        while (useOfMethode) {

            try {
                serverExchangeSocket[cptloop] = serverListeningSocket.accept();

                Thread acceptClientThread = new Thread(new AcceptClient(serverExchangeSocket[cptloop]));
                acceptClientThread.start();

                ServerLogger.info("New Client is connected " + serverExchangeSocket[cptloop]);
                registerClient(serverExchangeSocket[cptloop]);
                cptloop++;

            } catch (IOException e) {
                ServerLogger.severe("IOException " + e.toString());
                e.printStackTrace();
            }
            if (cptloop == 2) { //correct serverExchangeSocket if there are more clients
                useOfMethode = false;
            }
        }

        ServerLogger.info("Number of Clients connected " + cptloop);
        ServerLogger.info("Client one" + serverExchangeSocket[0]);
        ServerLogger.info("Client two" + serverExchangeSocket[1]);

        System.out.println("Le programme se trouve ici, prêt pour sendMusic");
        try {
            //!!!! ça marche, mais il faut separer les Clients
            sendMusicMenu(serverExchangeSocket[1], 0);
            sendMusicMenu(serverExchangeSocket[0], 1);
        } catch (IOException e) {
            ServerLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }


    }

    public void startServerLogger() {

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

    }

    public ServerSocket startServerSocket() {

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

                System.out.println("******************************************");
                System.out.println("I am listening ");

            } catch (IOException e) {
                e.printStackTrace();
                ServerLogger.severe("IO exception " + e.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ServerLogger.severe("Exception " + e.toString());
        }

        return serverListeningSocket;
    }

    public void findConnectionInfo() {

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

    public void registerClient(Socket serverExchangeSocket) {

        /**
         * @Thomas/Marina
         * Methode integre la recuperation de la fusee d'information du client (Regroupement d'infor de base du client)
         * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
         * de tous les clients
         */

        idClient++;
        ServerLogger.info("A client is connected");

        List<Object> clientPackage = new ArrayList<>();
        List<Object> incomingRocket = null;

        try {
            inputStream = serverExchangeSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            incomingRocket = (List<Object>) objectInputStream.readObject();
            ServerLogger.info("I've got from " + idClient + " this info " + incomingRocket);
        } catch (IOException | ClassNotFoundException e) {
            ServerLogger.severe("IO Exception or ClassNotFoundException in InputStream " + e.toString());
        }

        clientPackage.add(serverExchangeSocket);
        clientPackage.add(incomingRocket);


        //clientPackage est une liste d'objects !
        //avec à l'intérieur  :
        // 0 = IP
        // 1 = port clientClient
        // 2 = une liste de musique ListMusic
        // 3 = une liste de taille SizeMusic

        clientsList.add(clientPackage);


        // La suite est purement a des fins de test
        System.out.println();
        System.out.println("Receiving from client :");

        System.out.println("Taille de clientPackage : " + clientPackage.size());


        List<Object> clientRocket = (List<Object>) clientPackage.get(1);

        InetAddress ia = (InetAddress) clientRocket.get(0);
        System.out.println("InetAddress du client: " + ia);
        ServerLogger.info("Client IP :" + clientRocket.get(0));

        int portClientClient = (int) clientRocket.get(1);
        System.out.println("Port clientClient: " + portClientClient);

        System.out.println("Music list: " + clientRocket.get(2));
        System.out.println("Music Size: " + clientRocket.get(3));

    }

   // public List<Object> receiveMessageFromClient() throws IOException {

        /**
         * @Thomas
         * methode qui sert a recevoir le numero de selection audio venant du client
         * NE FONCTIONNE PAS EN THREAD !! UNIQUEMENT PREMIER CHOIX DISPONIBLE
         */

/*
        boolean methodeActiv = true;
        List<Object> incomingMessage = null;

        while (methodeActiv) {

            serverExchangeSocket = serverListeningSocket.accept();

            ServerLogger.info("A client is connected");

            try {
                InputStream inputStream2 = serverExchangeSocket.getInputStream();
                ObjectInputStream objectInputStream2 = new ObjectInputStream(inputStream2);
                incomingMessage = (List<Object>) objectInputStream2.readObject();

            } catch (IOException e) {
                ServerLogger.severe("IO Exception in ObjectInputStream " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            System.out.println("Receiving from client :");
            System.out.println("Audio selected from :");

            System.out.println(incomingMessage.get(0));

            System.out.println("Audio number :");
            System.out.println(incomingMessage.get(1));


        }


        return incomingMessage;
    } */


    public void findSelectedMusic(int numero) {


    }

    public void sendSomethingToClient(Socket socket, Object object) throws IOException {

        /**
         * @author Thomas/Marina
         * Methode qui va etre utilisee pour envoyer des informations/un objet a un client.
         */

        OutputStream outputStream = socket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(object);

    }


    public void sendMusicMenu(Socket socket, int clientN) throws IOException {

    /**
     * @author Thomas/Marina
     * methode utilisee pour envoyer a chaque client un menu de musique
     * Il enverra uniquement la liste contenant les musiques des autres clients.
     */
            String message = musicString(giveMusicList(clientN));

            if(message!="") {
                sendSomethingToClient(socket, message);
            }
            else{
                String sorry = "Aucun audio actuellement disponbile";
                sendSomethingToClient(socket,sorry);
            }
    }

    public List<String> giveMusicList(int clientNumber) {

        /**
         * @author Thomas
         * Methode qui va recuperer la liste des audios d'un client.
         */

        List<Object> tempList = new ArrayList<>();
        List<Object> packageList = new ArrayList<>();
        List<String> musicList = new ArrayList<>();

        for (int i = 0; i < clientsList.size(); i++) {

            if (clientNumber == i) {
                continue;
            } else {
                //On ouvre le bon clientList
                tempList = (List<Object>) clientsList.get(i);
                //clientListNr++;

                //On recupere la liste d'infos du client
                packageList = (List<Object>) tempList.get(1);

                //On selectionne la liste de music et on l'ajoute au menu des music
                musicList = (List<String>) packageList.get(2);
            }
        }

        return musicList;

    }

    public String musicString(List<String> music) {

        /**
         * @author Thomas
         * sert a retourner une liste de chanson
         * A MODIFIER POUR UTILISATION D UN CPT QUI NE COMMENCE PAS SYSTEMATIQUEMENT
         * A ZERO
         * sinon 1,2,3,1,2,1,2,3
         */


        String message = "";
        String messageTemp = "";

        int cpt = 0;


        Iterator<String> j = music.iterator();

        while (j.hasNext()) {

            messageTemp = j.next();
            messageTemp = messageTemp.substring(messageTemp.indexOf("myMusic") + 8, messageTemp.lastIndexOf("."));
            cpt++;

            message += (cpt + ". " + messageTemp + "\n");
        }


        return message;

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
            l = l.substring(l.indexOf("myMusic") + 8, l.lastIndexOf("."));
            System.out.println(cpt + ". " + l);
            cpt++;
        }

        System.out.println();

    }
}