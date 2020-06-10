package HES;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    /**
     * author Thomas/Marina
     * Classe server regroupant toutes les fonctionnalites lies a l utilisation d un serveur pour cet exercice
     */

    private final static Logger ServerLogger = Logger.getLogger("ServerLog");
    private List<Object> clientsList = new ArrayList<>();
    private int idClient = 0;
    private ServerSocket serverListeningSocket = null;
    private Socket [] serverExchangeSocket = new Socket [2];
    private InetAddress localAddress = null;
    private String interfaceName = "wlan1";
    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = dateFormat.format(currentDate.getTime());
    private InputStream inputStream = null;
    private ObjectInputStream objectInputStream = null;


    public Server() {

        startServerLogger();
        serverListeningSocket = startServerSocket();
        acceptNewClients();
        dispatchMusicMenuToClient();
        giveClientMusicInfo();

    }

    private void acceptNewClients(){

        /**
         * @author Marina
         * Methode servant a accepte avec un thread chaque nouveau client jusqu'a un maximum de 2.
         */

        boolean useOfMethode = true;
        int cptloop = 0;

        while (useOfMethode) {

            try {
                serverExchangeSocket[cptloop] = serverListeningSocket.accept();

                Thread acceptClientThread = new Thread(new AcceptClient(serverExchangeSocket[cptloop]));
                acceptClientThread.start();

                ServerLogger.info("New client is connected " + serverExchangeSocket[cptloop]);
                registerClient(serverExchangeSocket[cptloop]);
                cptloop++;


                if (cptloop == 2) {
                    useOfMethode = false;
                }
            }catch(IOException e){
                ServerLogger.severe("IO Exception while connecting with Client" + e.toString());
            }
        }

        ServerLogger.info("Number of clients connected " + cptloop);
        ServerLogger.info("Client one" + serverExchangeSocket[0]);
        ServerLogger.info("Client two" + serverExchangeSocket[1]);

    }

    private void dispatchMusicMenuToClient(){

        /**
         * @author Thomas
         * Methode servant a envoyer a chaque client une liste d audio de l'autre client
         */

        System.out.println("******************************************");
        System.out.println("Server send music list to clients");

        try {
            sendMusicMenu(serverExchangeSocket[1], 1);
            sendMusicMenu(serverExchangeSocket[0], 0);
        } catch (IOException e) {
            ServerLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }
    }

    private void giveClientMusicInfo(){

        /**
         * @author Thomas
         * Methode utilisee dans la version sequentielle uniquement
         * sert a envoyer a ClientOne les informations pour qu il puisse demande a ClientTwo l audio
         */

        try {
            int musicNumber = receiveMessageFromClient(serverExchangeSocket);
            sendSomethingToClient(serverExchangeSocket[0],findSelectedMusic(musicNumber));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startServerLogger() {

        /**
         * @author Marina
         * Methode servant a initier le logger pour le Server
         */

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

    private ServerSocket startServerSocket() {

        /**
         * @Thomas_et_Marina
         * Methode servant a initier les Sockets de Server et d'echange chez le Server,
         * le Serveur reste toujours en écoute et accepte tous les clients qui se connectent.
         */

        try {
            findConnectionInfo();
            try {
                serverListeningSocket = new ServerSocket(17257, 10, localAddress);
                ServerLogger.info("Default Timeout :" + serverListeningSocket.getSoTimeout());
                ServerLogger.info("Listening to Port :" + serverListeningSocket.getLocalPort());

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

    private void findConnectionInfo() {

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

    private void registerClient(Socket serverExchangeSocket) {

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
        clientsList.add(clientPackage);

        System.out.println("******************************************");
        System.out.println("Server receive information from client");

        List<Object> clientRocket = (List<Object>) clientPackage.get(1);
        InetAddress ia = (InetAddress) clientRocket.get(0);

        System.out.println("Client InetAddress: " + ia);
        ServerLogger.info("Client IP :" + clientRocket.get(0));
        int portClientClient = (int) clientRocket.get(1);
        System.out.println("Client Port for other clients: " + portClientClient);
        System.out.println("Client music list: " + clientRocket.get(2));

    }

    private int receiveMessageFromClient(Socket[] serverExchangeSocket) throws IOException {

        /**
         * @Thomas
         * methode qui sert a recevoir le numero de selection audio venant du client
         */


        boolean methodeActiv = true;
        int incomingMessage = 0;

        while (methodeActiv) {

            try {
                InputStream inputStream2 = serverExchangeSocket[0].getInputStream();
                ObjectInputStream objectInputStream2 = new ObjectInputStream(inputStream2);
                incomingMessage = (Integer) objectInputStream2.readObject();

            } catch (IOException e) {
                ServerLogger.severe("IO Exception in ObjectInputStream " + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(incomingMessage!=0){
                ServerLogger.info("Two clients are connected");
                ServerLogger.info("Audio selected");
                methodeActiv=false;
            }
        }

        return incomingMessage;
    }

    private void sendSomethingToClient(Socket socket, Object object) throws IOException {

        /**
         * @author Thomas/Marina
         * Methode qui va etre utilisee pour envoyer des informations/un objet a un client.
         */

        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
    }

    private List<Object> findSelectedMusic(int numero){

        /**
         * @author Thomas
         * Methode pour retrouver a l'aide d'un socket et d'un numero
         */

        List<Object>checkPackageList = null;
        List<Object>clientInfo = null;
        List<String>musicList = null;
        List<Integer>musicSize = null;

        List<Object>musicRocket = new ArrayList<>();

        checkPackageList = (List<Object>) clientsList.get(1);
        clientInfo = (List<Object>) checkPackageList.get(1);

        System.out.println(checkPackageList.size());

        musicList = (List<String>) clientInfo.get(2);
        musicSize = (List<Integer>) clientInfo.get(3);

        musicRocket.add(clientInfo.get(0));
        musicRocket.add(clientInfo.get(1));
        musicRocket.add(musicList.get(numero-1));
        musicRocket.add(musicSize.get(numero-1));

        return musicRocket;
    }

    private void sendMusicMenu(Socket socket, int clientN) throws IOException {

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
                String sorry = "No audio available";
                sendSomethingToClient(socket,sorry);
            }
    }

    private List<String> giveMusicList(int clientNumber) {

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
                tempList = (List<Object>) clientsList.get(i);
                packageList = (List<Object>) tempList.get(1);
                musicList = (List<String>) packageList.get(2);
            }
        }
        return musicList;
    }

    private String musicString(List<String> music) {

        /**
         * @author Thomas
         * Methode servant a retourner une liste d audio
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
}