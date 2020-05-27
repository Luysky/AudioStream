package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientAlpha  {

    /**
     * Thomas/Marina
     * Classe mere pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
     */

    protected final static Logger ClientLogger = Logger.getLogger("ClientLog");
    protected Socket exchangeSocket;
    protected String myMusicRepertory = "C://temp//AudioStream//myMusic";
    protected int portClientClient;

    private InetAddress localAddress = null;
    private String serverAddress = "192.168.0.15";
    private InetAddress serverIP;
    private int serverPort = 17257;
    private ServerSocket clientlisteningSocket;
    private Socket socketForOtherClient;
    private Socket exchangeSocketForOtherClient;
    private String incomingMessage = "";
    private InputStream inputStream = null;
    private List<String> myMusic = new ArrayList<>();
    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = formatter.format(currentDate.getTime());


    public ClientAlpha() { }

    protected void start (){

        /**
         * @author Thomas
         * En raison de difficulté a tester un client sur deux machines différentes, nous avons du simuler
         * cela en creant deux classes clients.
         * La methode start est un ersatz de constructeur qui sera recupere par chaque client par heritage.
         * Elle sert a initier les sockets,
         * envoyer la liste d audio au server
         * lire le menu recu par le server
         */

        startClientLogger();

        try {
            startClientSockets();
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }

        sendSomethingToSomeone(exchangeSocket, collectMyInfo());
        readIncomingMessage(exchangeSocket);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    protected int musicChoice() {
        /**
         * @author Thomas
         * methode servant a demander a l'utilisateur le numero de l'audio souhaite
         */
        boolean check=false;
        int myChoice = 0;

        do {
            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter the number of your song ");

            try {
                myChoice = scan.nextInt();
                check=true;
            } catch (InputMismatchException e) {
                System.out.println("Number only !");
                scan.next();
            } catch (IndexOutOfBoundsException e) {
                System.out.println("No such number !");
                scan.next();
            }
        }
        while (check==false);

        return myChoice;
    }

    private void startClientLogger() {

        /**
         * @author Marina
         * Methode servant a initier le logger pour les clients
         */

        FileHandler fh = null;

        try {
            fh = new FileHandler("C://temp//AudioStream//Client " + dateNow + ".log", false);
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }
        CustomFormatter customFormatter = new CustomFormatter();
        fh.setFormatter(customFormatter);

        ClientLogger.addHandler(fh);
        ClientLogger.setLevel(Level.INFO);
        ClientLogger.info("********************** Program starts ******************");

    }

    private void startClientSockets() throws IOException {

        /**
         * @author_Thomas_et_Marina
         * Methode servant a initier les Socket de Server et d'echange pour les clients
         */

        try {

            serverIP = InetAddress.getByName(serverAddress);
            ClientLogger.info("Get the address of the server : "+ serverIP);
            exchangeSocket = new Socket(findIpAddress(), serverPort);
        } catch (NullPointerException e) {
            System.out.println("Connection interrupted with the server");
        }
    }

    protected void sendSomethingToSomeone(Socket exchangeSocket, Object object) {

        /**
         * @author Thomas
         * methode servant a envoyer un objet a un autre client
         */

        try {
            ClientLogger.info("I am connected to " + exchangeSocket.getPort());
            OutputStream outputStream = exchangeSocket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            ClientLogger.info("Message sent" + object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<Object> receiveClientAudioRequest() {

        /**
         * author Thomas
         * Methode servant a la recuperation de la demande de chanson d'un client.
         * On le recupere et on l'enregistre dans une List d'object
         */

        System.out.println("******************************************");
        System.out.println("Receiving client request");

        List<Object> clientMessage = null;
        try {

            clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
            socketForOtherClient = clientlisteningSocket.accept();
            ClientLogger.info("Socket for Other Client created " + socketForOtherClient);

            try {
                InputStream inputStream = socketForOtherClient.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                clientMessage = (List<Object>) objectInputStream.readObject();

            } catch (IOException e) {
                e.printStackTrace();
                ClientLogger.severe("IO exception " + e.toString());
            }
            clientlisteningSocket.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clientMessage;
    }

    protected void listeningToClients(List<Object>infoSong) {

        /**
         * @author Thomas/Marina
         * Methode qui sert a recevoir depuis le server le menu des audios
         * c'est egalement ici que l'on fait le choix de l'audio que l'on veut
         */

            try {
                clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
                socketForOtherClient = clientlisteningSocket.accept();

                System.out.println("******************************************");
                System.out.println("Transfering audio to other client");
                System.out.println("Sharing audio : "+infoSong.get(2));
                ClientLogger.info("Transfering audio "+infoSong.get(2)+" to ohter client");

                File myFile = new File((String) infoSong.get(2));
                byte[] mybytearray = new byte[(int) myFile.length()];

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray, 0, mybytearray.length);

                OutputStream os = socketForOtherClient.getOutputStream();
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();

            } catch (IOException e) {
                e.printStackTrace();
                ClientLogger.severe("IO exception " + e.toString());
            }
    }



    protected void connectToOtherClient(InetAddress ipClient, int port){

        /**
         * author Marina
         * Methode servant a recevoir d'un client l'audio par le biais d'un bufferedInputstream
         * l audio est joue directement depuis cette methode
         */

        System.out.println("IP " + ipClient + " port " + port);
        try {
            exchangeSocketForOtherClient = new Socket(ipClient, port);
            ClientLogger.info("Client connected to other Client : socket" + exchangeSocketForOtherClient);
        } catch (IOException e) {
            ClientLogger.severe("IOException while connection to Other Client" + e.toString());
            e.printStackTrace();
        }

        boolean musicOn=true;

        do {

            try {
                BufferedInputStream bis = new BufferedInputStream(exchangeSocketForOtherClient.getInputStream());
                System.out.println("******************************************");
                System.out.println("Client starts receiving audio");

                try {
                    AudioPlayer player = new AudioPlayer(bis);
                    player.play();
                    try {
                        System.out.println("Enjoy !");
                        Thread.sleep(player.clip.getMicrosecondLength());
                        ClientLogger.info("Listening music");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                ClientLogger.severe("IOException while trying to get InputStream from other Client" + e.toString());
                e.printStackTrace();
            }
        }
        while (musicOn);


    }

    private void readIncomingMessage(Socket socket) {

        /**
         * @author Thomas/Marina
         * Methode qui va permettre de recuperer des informations entrantes venant du server.
         */

        System.out.println("******************************************");
        System.out.println("Incoming message from server");

        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            incomingMessage = (String) objectInputStream.readObject();

            System.out.println();
            System.out.println("Available audio :");
            System.out.println(incomingMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    protected List<Object> readMessageConvertToList(Socket socket) {

        /**
         * @author Thomas/Marina
         * Methode qui va permettre de recuperer des informations entrantes venant du server.
         */

        List<Object>incomingMessage = null;
        System.out.println("******************************************");
        System.out.println("Information about other client received");

        try {
            InputStream iS = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(iS);
            incomingMessage = (List<Object>) objectInputStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return incomingMessage;

    }

    private InetAddress findIpAddress() {
        /**
         * @author Thomas
         * Methode qui va servir à determiner automatiquement l'adresse ip du client et son interface
         */

        InetAddress localAddress = null;
        NetworkInterface ni;
        try {
            ni = NetworkInterface.getByName(findInterface());
            Enumeration<InetAddress> LocalAddress = ni.getInetAddresses();

            while (LocalAddress.hasMoreElements()) {
                InetAddress ia = LocalAddress.nextElement();
                if (!(ia instanceof Inet6Address) && !(ia.isLoopbackAddress())) {
                    localAddress = ia;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return localAddress;
    }

    private String findInterface() throws SocketException {

        /**
         * @author Thomas
         * Methode qui va detecter automatiquement l'interface wlan utilisee par le client
         * On va forcer l'interface de type wlan.
         */

        String myInterface = "";

        Enumeration<NetworkInterface> allNi;
        allNi = NetworkInterface.getNetworkInterfaces();

        while (allNi.hasMoreElements()) {
            NetworkInterface nix = allNi.nextElement();

            if (nix.isUp() == true) {

                if (nix.getName().contains("w")) {
                    myInterface = nix.getName();
                }
            }
        }
        return myInterface;
    }

    private List<String> searchMyMusic() {

        /**
         * @author Thomas
         * Methode qui va repertorier tous les fichiers wav se trouvant dans le repertoire du client
         */

        List<String> myMusic = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(myMusicRepertory))) {

            myMusic = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".wav")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return myMusic;
    }

    private List<Integer> searchSizesMySongs() {

        /**
         * @author Thomas
         * Methode qui va chercher la taille necessaire pour les array de byte
         * de chaque chanson du client
         */

        List<Integer> sizesMySongs = new ArrayList<>();
        int size = 0;
        myMusic = searchMyMusic();

        for (int i = 0; i < myMusic.size(); i++) {

            File myFile = new File(myMusic.get(i));
            byte[] mybytearray = new byte[(int) myFile.length()];
            size = mybytearray.length;
            sizesMySongs.add(size);
        }
        return sizesMySongs;

    }

    private List<Object> collectMyInfo() {

        /**
         * @author Thomas
         * Methode qui va charger les infos necessaires pour le server dans une List
         */

        List<Object> myCollectedInfo = new ArrayList<>();
        myCollectedInfo.add(findIpAddress());
        myCollectedInfo.add(portClientClient);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());

        return myCollectedInfo;
    }


    protected void askClientForAnAudio(){

        /**
         * @author Thomas
         * Methode regroupant l ensemble des methodes et demarches necessaires pour la demande
         * aupres d un autre client d un audio jusqu a sa reception et a son ecoute
         */

        List<Object> clientMusicInfo =  readMessageConvertToList(exchangeSocket);
        InetAddress ipOfOtherClient = (InetAddress) clientMusicInfo.get(0);
        int port = (Integer) clientMusicInfo.get(1);

        Socket comSocket = null;

        try {
            comSocket = new Socket(ipOfOtherClient, port);
            ClientLogger.info("Client connected to other Client : socket" + ipOfOtherClient);
        } catch (IOException e) {
            ClientLogger.severe("IOException while connection to Other Client" + e.toString());
            e.printStackTrace();
        }

        sendSomethingToSomeone(comSocket,clientMusicInfo);
        connectToOtherClient(ipOfOtherClient, port);

    }

    protected void giveClientAnAudio(){

        /**
         * author Thomas
         * Methode regroupant l ensemble des methodes et des demarches necessaires
         * pour la reception d un requete d un autre client pour un audio jusqu a sa diffusion
         */

        List<Object>infoSong = receiveClientAudioRequest();
        listeningToClients(infoSong);
    }

}