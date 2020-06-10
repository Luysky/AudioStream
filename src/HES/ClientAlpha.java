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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private Socket exchangeSocket;
    private String myMusicRepertory = "C://temp//AudioStream//myMusic";
    private int portClientClient;

    private InetAddress localAddress = null;
    private String serverAddress = "192.168.0.15";
    private InetAddress serverIP;
    private int serverPort = 17257;

    private ServerSocket clientlisteningSocket;
    private Socket socketForOtherClient;
    private ServerSocket serverSocketForClient;

    private ArrayList<ClientManager> clients = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(4); // menages up to 4 clients
    private ClientManager clientThread;

    // For ClientClient communication
    private String line = null;
    private BufferedReader br = null;
    private BufferedReader is = null;
    private InputStream inputForMusic;
    private PrintWriter os = null;
    private Scanner scan;

    // For music
    private String pathOfChosenSong = "";
    private Integer sizeOfChosenSong = 0;
    private InetAddress ipOfOwnerOfSong;
    private int portOfOwnerOfSong;
    private Socket socketForMusic;

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

    protected void listenToMusic(InputStream is) {

        /**
         * author Marina
         * Methode servant a recevoir d'un client l'audio par le biais d'un bufferedInputstream
         * l audio est joue directement depuis cette methode
         */


        System.out.println("******************************************");
        System.out.println("Client starts receiving audio");

        AudioPlayer player = null;
        try {
            player = new AudioPlayer(is);
            player.play();
            System.out.println("Play is on");
            try {
                System.out.println("Enjoy !");
                Thread.sleep(player.clip.getMicrosecondLength());
                ClientLogger.info("Listening music");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

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

    protected List<String> searchMyMusic() {

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

    private void createEchoServer(InetAddress serverIP, int port) throws IOException {

        try {
            clientlisteningSocket = new ServerSocket(port, 10, serverIP);
        } catch (IOException e) {
            throw new RuntimeException("Could not create ServerSocket ", e);
        }

        while(true) {
            synchronized (clientlisteningSocket) {
                try {
                    socketForOtherClient = clientlisteningSocket.accept();
                    ClientLogger.info("Socket for ClientClient connection " + socketForOtherClient);
                } catch (IOException e) {
                    ClientLogger.severe("IOException while accepting Clients" + e.toString());
                }
            }
            clientThread = new ClientManager(socketForOtherClient, clients);
            clients.add(clientThread);
            pool.execute(clientThread);
        }


    }

    private Socket connectToEchoServer(InetAddress serverIP, int serverPort){
        try {
            socketForOtherClient = new Socket(serverIP, serverPort);
            ClientLogger.info("Socket to connect to other Client : " + socketForOtherClient);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("IO Exception");
        }

        return socketForOtherClient;

    }

    private void talkToOthers(Socket s){

        br = new BufferedReader(new InputStreamReader(System.in));
        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream(), true);
            inputForMusic = new BufferedInputStream(s.getInputStream());
        }catch(IOException e){
            ClientLogger.severe("IOException while talking" + e);
        }
        ClientLogger.info("Client Address : " + s.getInetAddress() + ", port " + s.getPort());
        System.out.println("Enter Data to echo Server ( Enter QUIT to end):");


        String response = null;
        try {
            line = br.readLine();

            while (line.compareTo("QUIT") != 0) {

                if(line.matches(".*\\d.*")){
                    os.println(line);
                    os.flush();
                    System.out.println("I asked to play a song with a number");
                    listenToMusic(inputForMusic);
                }else if(line.startsWith("play music")){
                    os.println(line);
                    os.flush();
                    System.out.println("I asked to play music to all ");
                    listenToMusic(inputForMusic);
                }else {
                    os.println(line);
                    os.flush();
                    response = is.readLine();
                    System.out.println("Server Response : " + response);
                }

                line = br.readLine();
            }

            String msg = "I am ready to listen to music ";
            os.println(msg);
            os.flush();
            String serverMsg = is.readLine();
            System.out.println("Message from server " + serverMsg);
            listenToMusic(inputForMusic);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Socket read Error");
        } finally {

            try {
                is.close();
                os.close();
                br.close();
                s.close();
                System.out.println("All is closed");
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Connection Closed");

        }


    }

    public void whoAreYou(){

        /**
         * @author Thomas
         * Methode servant a effectuer la demonstration de maniere sequentielle.
         * Le premier client qu on utilise doit OBLIGATOIREMENT se definir comme nr. 1 Listener Client
         * Le server a ete defini dans ce sens la.
         */


        System.out.println("******************************************");
        System.out.println("Welcome to AudioStream!");
        System.out.println("Please select your role : \n 1. Listener Client \n 2. Broadcaster Client ");
        System.out.println("******************************************");

        int clientNumber = myChoice();

        switch (clientNumber){

            case 1 :    System.out.println("You have selected Client Listener");
                        System.out.println("******************************************");
                        myMusicRepertory="C://temp//AudioStream//myMusic";
                        portClientClient = 25245;

                        start();
                        sendSomethingToSomeone(exchangeSocket, musicChoice());
                        List<Object> clientMusicInfo =  readMessageConvertToList(exchangeSocket);
                        ipOfOwnerOfSong = (InetAddress) clientMusicInfo.get(0);
                        portOfOwnerOfSong = (Integer) clientMusicInfo.get(1);
                        pathOfChosenSong = (String)clientMusicInfo.get(2);
                        sizeOfChosenSong = (Integer)clientMusicInfo.get(3);

                        System.out.println("clientMusicInfo" + clientMusicInfo);
                        socketForMusic = connectToEchoServer(ipOfOwnerOfSong, portOfOwnerOfSong);
                        System.out.println("Socket to talk and listen to music" + socketForMusic);
                        talkToOthers(socketForMusic);

                        break;

            case 2 :    System.out.println("You have selected Client Broadcaster");
                        System.out.println("******************************************");
                        myMusicRepertory="C://temp//AudioStream2//myMusic";
                        portClientClient = 25250;

                        start();
                        try {
                        createEchoServer(findIpAddress(), portClientClient);
                        } catch (IOException e) {
                        ClientLogger.severe("IOException while creating ClientServer " + e.toString());
                        }

                        break;

            default:    System.out.println("Sorry, something went wrong please restart");
                        System.out.println("******************************************");

        }

    }

    private int myChoice() {

        /**
         * @Thomas
         * methode qui permet de recuperer un choix d'un client dans la console
         */

        int myChoice = 0;
        boolean check = false;

        do {
            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter the number of your song ");

            try {
                myChoice = scan.nextInt();

                if(myChoice<1 ||myChoice>2){
                    do{
                        System.out.println("Choose only 1 or 2! Please try again.");
                        myChoice = scan.nextInt();
                    }
                    while (myChoice<1||myChoice>2);

                }

                check=true;
            } catch (InputMismatchException e) {
                System.out.println("Number only !");
                scan.next();
            }
        }
        while (check==false);

        return myChoice;
    }

}