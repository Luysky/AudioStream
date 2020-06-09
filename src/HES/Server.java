package HES;


import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * author Thomas/Marina
 * Classe server regroupant toutes les fonctionnalites lies a l utilisation d un serveur pour cet exercice
 */

public class Server {

    private LogFiles logger;

    private List<Object> clientsList = new ArrayList<>();
    private int idClient = 0;
    private ServerSocket serverListeningSocket;
    private Socket [] serverExchangeSocket = new Socket [2];
    private InetAddress localAddress = null;
    private String interfaceName = "wlan1";
    private InputStream inputStream = null;
    private ObjectInputStream objectInputStream = null;


    public Server() {

        logger = LogFiles.getInstance();
        logger.startLog();
        logger.logClientServer.info("******************** Program starts ********************");

        serverListeningSocket = startServerSocket();
        acceptNewClients();
        dispatchMusicMenuToClient();
        giveClientMusicInfo();

    }

    /**
     * @author Marina
     * Methode servant a accepte avec un thread chaque nouveau client jusqu'a un maximum de 2.
     */

    private void acceptNewClients(){

        boolean useOfMethode = true;
        int cptloop = 0;

        while (useOfMethode) {

            try {
                serverExchangeSocket[cptloop] = serverListeningSocket.accept();

                Thread acceptClientThread = new Thread(new AcceptClient(serverExchangeSocket[cptloop]));
                acceptClientThread.start();

                logger.logClientServer.warning("New client is connected " + serverExchangeSocket[cptloop]);
                registerClient(serverExchangeSocket[cptloop]);
                cptloop++;

            } catch (IOException e) {
                logger.logClientServer.severe("IOException " + e.toString());
                e.printStackTrace();
            }
            if (cptloop == 2) {
                useOfMethode = false;
            }
        }

        logger.logClientServer.info("Number of clients connected " + cptloop);
        logger.logClientServer.info("Client one" + serverExchangeSocket[0]);
        logger.logClientServer.info("Client two" + serverExchangeSocket[1]);

    }

    /**
     * @author Thomas
     * Methode servant a envoyer a chaque client une liste d audio de l'autre client
     */

    private void dispatchMusicMenuToClient(){

        System.out.println("******************************************");
        System.out.println("Server send music list to clients");

        try {

            sendMusicMenu(serverExchangeSocket[0], 0);
            sendMusicMenu(serverExchangeSocket[1], 1);

        } catch (IOException e) {
            logger.logClientServer.severe("IOException " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * @author Thomas
     * Methode utilisee dans la version sequentielle uniquement
     * sert a envoyer a ClientOne les informations pour qu il puisse demande a ClientTwo l audio
     */

    private void giveClientMusicInfo(){

        try {
            int musicNumber = receiveMessageFromClient();
            sendSomethingToClient(findTheSocket(),findSelectedMusic(musicNumber));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * @author Thomas
     * @author Marina
     * Methode servant a initier les Sockets de Server et d'echange chez le Server,
     * le Serveur reste toujours en écoute et accepte tous les clients qui se connectent.
     */

    private ServerSocket startServerSocket() {

        try {
            findConnectionInfo();
            try {
                serverListeningSocket = new ServerSocket(17257, 10, localAddress);
                logger.logClientServer.warning("New server listening socket");
                logger.logClientServer.info("Default Timeout :" + serverListeningSocket.getSoTimeout());
                logger.logClientServer.info("Listening to Port :" + serverListeningSocket.getLocalPort());
                System.out.println();
                System.out.println("******************************************");
                System.out.println("Server listening to clients");
            } catch (IOException e) {
                e.printStackTrace();
                logger.logClientServer.severe("IO exception " + e.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.logClientServer.severe("Exception " + e.toString());
        }
        return serverListeningSocket;
    }

    /**
     * @author Thomas
     * Methode qui va faire les recherches de l'interface et de l'adresse IP utilisé par le server.
     * De cette manière le code fonctionne sur n'importe quelle machine sans réglage supplémentaire.
     */

    private void findConnectionInfo() {

        NetworkInterface ni = null;
        try {
            ni = NetworkInterface.getByName(interfaceName);
            logger.logClientServer.warning("New NetworkInterface available");
        } catch (SocketException e) {
            System.out.println("Connection Timed out");
            logger.logClientServer.severe("Connection Timed out");
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

    /**
     * @author Thomas
     * @author Marina
     * Methode integre la recuperation de la fusee d'information du client (Regroupement d'infor de base du client)
     * ainsi que le stockage des elements dans une nouvelle arrayList qui regroupera toutes les infos
     * de tous les clients
     */

    private void registerClient(Socket serverExchangeSocket) {

        idClient++;
        logger.logClientServer.warning("A client is connected");
        List<Object> clientPackage = new ArrayList<>();
        List<Object> incomingRocket = null;

        try {
            inputStream = serverExchangeSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            incomingRocket = (List<Object>) objectInputStream.readObject();
            logger.logClientServer.info("I've got from " + idClient + " this info " + incomingRocket);
        } catch (IOException | ClassNotFoundException e) {
            logger.logClientServer.severe("IO Exception or ClassNotFoundException in InputStream " + e.toString());
        }

        clientPackage.add(serverExchangeSocket);
        clientPackage.add(incomingRocket);
        clientsList.add(clientPackage);

        System.out.println("******************************************");
        System.out.println("Server receive information from client");

        List<Object> clientRocket = (List<Object>) clientPackage.get(1);
        InetAddress ia = (InetAddress) clientRocket.get(0);

        System.out.println("Client InetAddress: " + ia);
        logger.logClientServer.info("Client IP :" + clientRocket.get(0));
        int portClientClient = (int) clientRocket.get(1);
        System.out.println("Client Port for other clients: " + portClientClient);
        System.out.println("Client music list: " + clientRocket.get(2));

    }

    /**
     * @author Thomas
     * methode qui sert a recevoir le numero de selection audio venant du client
     */

    private int receiveMessageFromClient() {

        boolean methodeActiv = true;
        int incomingMessage = 0;

        while (methodeActiv) {

            try {

                InputStream inputStream3 = findTheSocket().getInputStream();
                ObjectInputStream objectInputStream2 = new ObjectInputStream(inputStream3);
                incomingMessage = (Integer) objectInputStream2.readObject();

            } catch (IOException e) {
                logger.logClientServer.severe("IO Exception in ObjectInputStream " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(incomingMessage!=0){
                logger.logClientServer.info("A client is connected");
                logger.logClientServer.info("Audio selected");
                methodeActiv=false;
            }
        }

        return incomingMessage;
    }

    /**
     * @author Thomas
     * Methode qui va trouver le socket que l on doit utiliser lors d un envoi.
     * Le socket est determine par le choix utilisateur fait au debut du programme avec clientRole
     */

    private Socket findTheSocket(){

        int clientRole;
        int number = 0;
        Socket clientSocket;
        do {

            List<Object>checkPackageList;
            List<Object>rocketList;

            checkPackageList = (List<Object>) clientsList.get(number);
            clientSocket = (Socket) checkPackageList.get(0);
            rocketList = (List<Object>) checkPackageList.get(1);
            clientRole = (Integer)rocketList.get(4);
            number++;
        }
        while (clientRole!=2);

        int verif = clientSocket.getPort();
        int socket1 = serverExchangeSocket[0].getPort();

        if(verif==socket1){
            return serverExchangeSocket[1];
        }
        else{
            return serverExchangeSocket[0];
        }

    }


    /**
     * @author Thomas/Marina
     * Methode qui va etre utilisee pour envoyer des informations/un objet a un client.
     */

    private void sendSomethingToClient(Socket socket, Object object) throws IOException {

        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
    }

    /**
     * @author Thomas
     * Methode pour retrouver l'audio a l'aide d'un numero
     * Le bon socket est determine par le role enregistre au debut du programme.
     */

    private List<Object> findSelectedMusic(int numero){

        int clientRole;
        int number = 0;
        Socket clientSocket = null;
        do {

            List<Object>checkPackageList;
            List<Object>rocketList;

            checkPackageList = (List<Object>) clientsList.get(number);
            rocketList = (List<Object>) checkPackageList.get(1);
            clientRole = (Integer)rocketList.get(4);
            number++;
        }
        while (clientRole!=2);

        List<Object>checkPackageList;
        List<Object>clientInfo;
        List<String>musicList;
        List<Integer>musicSize;

        List<Object>musicRocket = new ArrayList<>();

        checkPackageList = (List<Object>) clientsList.get(number-1);
        clientInfo = (List<Object>) checkPackageList.get(1);
        musicList = (List<String>) clientInfo.get(2);
        musicSize = (List<Integer>) clientInfo.get(3);

        musicRocket.add(clientInfo.get(0));
        musicRocket.add(clientInfo.get(1));
        musicRocket.add(musicList.get(numero-1));
        musicRocket.add(musicSize.get(numero-1));

        return musicRocket;
    }


    /**
     * @author Thomas/Marina
     * methode utilisee pour envoyer a chaque client un menu de musique
     * Il enverra uniquement la liste contenant les musiques des autres clients.
     */

    private void sendMusicMenu(Socket socket, int clientN) throws IOException {

        String message = musicString(giveMusicList(clientN));

        if(!message.equals("")) {
            sendSomethingToClient(socket, message);
        }
        else{
            String sorry = "No audio available";
            sendSomethingToClient(socket,sorry);
        }
    }

    /**
     * @author Thomas
     * Methode qui va recuperer la liste des audios d'un client.
     */

    private List<String> giveMusicList(int clientNumber) {

        List<Object> tempList;
        List<Object> packageList;
        List<String> musicList = new ArrayList<>();

        for (int i = 0; i < clientsList.size(); i++) {

            if (clientNumber == i) {
                continue;
            } else {
                tempList = (List<Object>) clientsList.get(i);
                packageList = (List<Object>) tempList.get(1);
                musicList = (List<String>) packageList.get(2);
            }
        }
        return musicList;
    }

    /**
     * @author Thomas
     * Methode servant a retourner une liste d audio
     */

    private String musicString(List<String> music) {

        String message = "";
        String messageTemp;

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
}