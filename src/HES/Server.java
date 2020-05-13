package HES;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server implements Runnable {

    private final static Logger ServerLogger = Logger.getLogger("ServerLog");

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

    private String clientActive = null;
    private String clientAnswer = null;



    public Server() {

        startServerLogger();
        //sendInfoFromClient();

        //Si tu actives uniquement run() dans server et client la musique se lance
        //NE PAS ACTIVER EN MEME TEMPS QUE LE RESTE DREPRECATED
        //run();

        //Thread t1 = new Thread(new Server());

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


        try {
            receiveMessageFromClient();
        } catch (IOException e) {
            e.printStackTrace();
        };


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

                boolean useOfMethode = true;
                int cptloop = 0;

                //while (true) {
                while (useOfMethode){
                    serverExchangeSocket = serverListeningSocket.accept();

                    System.out.println("******************************************");

                    System.out.println("I am listening ");
                    Thread acceptClientThread = new Thread(new AcceptClient(serverExchangeSocket, ClientNumber));
                    ClientNumber++;
                    acceptClientThread.start();
                    receiveInfoFromClient();


                    sendMusicMenu();

                    cptloop++;

                    if (cptloop==2){
                        useOfMethode=false;
                    }


                    serverExchangeSocket.close();

                    //run();
                    //checkIfClientActif();
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


        //InputStream inputStream = null;
        try {
            inputStream = serverExchangeSocket.getInputStream();
        } catch (IOException e) {
            ServerLogger.severe("IO Exception in InputStream " + e.toString());
        }

        // create a DataInputStream so we can read data from it.

        //ObjectInputStream objectInputStream = null;
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


    }

    public List<Object> receiveMessageFromClient() throws IOException {

        /**
         * @Thomas
         * methode qui sert a recevoir le numero de selection audio venant du client
         * NE FONCTIONNE PAS EN THREAD !! UNIQUEMENT PREMIER CHOIX DISPONIBLE
         */


        boolean methodeActiv = true;
        List<Object> incomingMessage = null;

        while(methodeActiv) {

            serverExchangeSocket = serverListeningSocket.accept();

            ServerLogger.info("A client is connected");

            try {
                InputStream  inputStream2 = serverExchangeSocket.getInputStream();
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
    }


    public void findSelectedMusic(int numero){



    }


    public void checkIfClientActif() throws IOException {

        /**
         * @author Thomas
         * methode qui va permettre de faire un check de presence de chaque client
         * chaque 10 secondes.
         *
         * INACTIVE NECESSITE UN THREAD
         */

        for(int i = 0; i<clientsList.size(); i++) {

            InetAddress clientIp = retreivedIpFromClient(clientsList, i);
            int port = retreivedPortFromClient(clientsList, i);


            sendCheckToClient(clientIp,port);

        }

    }


    public void sendCheckToClient (InetAddress clientIp, int clientPort){

        /**
         * @author Thomas
         * methode qui va servir au server de control de la connection d'un client
         * Automatiquement chaque 10 seconces le server va echanger des messages avec tous les clients
         * il va simplement envoyer un message pout avec la lettre a
         * et le client va automatiquement lui envoyer en retour cettte lettre
         * si le client se deconnecte la methode va le detecter et va generer un message d'erreur
         *
         * INACTIVE CAR NECESSITE UN THREAD
         * A MODIFIER POUR INTEGRER AVEC LIST MUSIC, ETC
         */


        try {
            serverExchangeSocket = serverListeningSocket.accept();
            InputStream iS = serverExchangeSocket.getInputStream();
            InputStreamReader iSr = new InputStreamReader(iS);
            BufferedReader buffin = new BufferedReader(iSr);
            PrintWriter pout = new PrintWriter(serverExchangeSocket.getOutputStream());

            try {
                while (true) {
                    clientActive="a";
                    pout.println(clientActive);
                    pout.flush();
                    System.out.println("Message send to client : "+clientActive);
                    clientAnswer = buffin.readLine();
                    System.out.println("Received message from client : " + clientAnswer);

                    Thread.sleep(10 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pout.flush();
            pout.close();

        }
        catch (SocketException e) {
            System.out.println("Client is unreachable");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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


    public void sendMusicMenu() throws IOException {

        /**
         * @author Thomas
         * methode utilisee pour envoyer a chaque client un menu de musique
         * Il enverra uniquement la liste contenant les musiques des autres clients.
         */

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


    @Override
    public void run() {

        /**
         * @author Thomas
         * methode inactive tentative d'utilisation des threads
         */

        try {

            sendMusicMenu();
            checkIfClientActif();


            System.out.println("J'ai passé dans le thread");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}