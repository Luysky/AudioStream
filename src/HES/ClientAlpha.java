package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Thomas
 * @author Marina
 * Classe mere pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
 */

public class ClientAlpha  {

    private Socket exchangeSocket;
    private String myMusicRepertory = "C://temp//AudioStream//myMusic";
    private int portClientClient;

    private InetAddress localAddress = null;
    private InetAddress serverIP;
    private int serverPort = 17257;
    private ServerSocket clientlisteningSocket;
    private Socket socketForOtherClient;
    private Socket exchangeSocketForOtherClient;
    private String incomingMessage = "";
    private InputStream inputStream = null;
    private List<String> myMusic = new ArrayList<>();
    private int myRole = 0;


    public ClientAlpha() {

        //whoAreYou();

    }

    /**
     * @author Thomas
     * En raison de difficulté a tester un client sur deux machines différentes, nous avons du simuler
     * cela en creant deux classes clients.
     * La methode start est un ersatz de constructeur qui sera recupere par chaque client par heritage.
     * Elle sert a initier les sockets,
     * envoyer la liste d audio au server
     * lire le menu recu par le server
     */

    private void start (){

        try {
            startClientSockets();
        } catch (IOException e) {
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

    /**
     * @author Thomas
     * methode servant a demander a l'utilisateur le numero de l'audio souhaite
     */

    private int musicChoice() {

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
        while (!check);

        return myChoice;
    }

    /**
     * @author_Thomas
     * @author Marina
     * Methode servant a initier les Socket de Server et d'echange pour les clients
     */

    private void startClientSockets() throws IOException {

        try {
            serverIP = findIpAddress();
            System.out.println("Get the address of the server : "+ serverIP);
            exchangeSocket = new Socket(findIpAddress(), serverPort);
        } catch (NullPointerException e) {
            System.out.println("Connection interrupted with the server");
        }
    }

    /**
     * @author Thomas
     * methode servant a envoyer un objet a un autre client
     */

    private void sendSomethingToSomeone(Socket exchangeSocket, Object object) {

        try {
            System.out.println("I am connected to " + exchangeSocket.getPort());
            OutputStream outputStream = exchangeSocket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            System.out.println("Message sent" + object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author Thomas
     * Methode servant a la recuperation de la demande de chanson d'un client.
     * On le recupere et on l'enregistre dans une List d'object puis on écoute le morceau reçu
     */

    private List<Object> receiveClientRequest() {

        System.out.println("******************************************");
        System.out.println("Receiving client request");

        List<Object> clientMessage = null;
        try {

            clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
            socketForOtherClient = clientlisteningSocket.accept();
            System.out.println("Socket for Other Client created " + socketForOtherClient);

            try {
                InputStream inputStream = socketForOtherClient.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                clientMessage = (List<Object>) objectInputStream.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            }
            clientlisteningSocket.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clientMessage;

    }

    /**
     * @author Thomas
     * @author Marina
     * Methode qui sert a diffuser depuis le client l audio qui a ete demande
     */

    private void transferAudio(List<Object>infoSong) {

            try {
                clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
                socketForOtherClient = clientlisteningSocket.accept();

                System.out.println("******************************************");
                System.out.println("Transfering audio to other client");
                System.out.println("Sharing audio : "+infoSong.get(2));
                System.out.println("Transfering audio "+infoSong.get(2)+" to ohter client");

                File myFile = new File((String) infoSong.get(2));
                byte[] mybytearray = new byte[(int) myFile.length()];

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray, 0, mybytearray.length);

                OutputStream os = socketForOtherClient.getOutputStream();
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * @author Marina
     * Methode servant a recevoir d'un client l'audio par le biais d'un bufferedInputstream
     * l audio est joue directement depuis cette methode
     */

    private void connectToOtherClient(InetAddress ipClient, int port){

        System.out.println("IP " + ipClient + " port " + port);
        try {
            exchangeSocketForOtherClient = new Socket(ipClient, port);
            System.out.println("Client connected to other Client : socket" + exchangeSocketForOtherClient);
        } catch (IOException e) {
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
                        System.out.println("Listening music");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (musicOn);


    }

    /**
     * @author Thomas
     * @author Marina
     * Methode qui va permettre de recuperer des informations entrantes venant du server.
     */

    private void readIncomingMessage(Socket socket) {

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

    /**
     * @author Thomas/Marina
     * Methode qui va permettre de recuperer des informations entrantes venant du server.
     */

    private List<Object> readMessageConvertToList(Socket socket) {

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

    /**
     * @author Thomas
     * Methode qui va servir à determiner automatiquement l'adresse ip du client et son interface
     */

    private InetAddress findIpAddress() {

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

    /**
     * @author Thomas
     * Methode qui va detecter automatiquement l'interface wlan utilisee par le client
     * On va forcer l'interface de type wlan.
     */

    private String findInterface() throws SocketException {

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

    /**
     * @author Thomas
     * Methode qui va repertorier tous les fichiers wav se trouvant dans le repertoire du client
     */

    private List<String> searchMyMusic() {

        List<String> myMusic = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(myMusicRepertory))) {

            myMusic = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".wav")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return myMusic;
    }

    /**
     * @author Thomas
     * Methode qui va chercher la taille necessaire pour les array de byte
     * de chaque chanson du client
     */

    private List<Integer> searchSizesMySongs() {

        List<Integer> sizesMySongs = new ArrayList<>();
        int size;
        myMusic = searchMyMusic();

        for (int i = 0; i < myMusic.size(); i++) {

            File myFile = new File(myMusic.get(i));
            byte[] mybytearray = new byte[(int) myFile.length()];
            size = mybytearray.length;
            sizesMySongs.add(size);
        }
        return sizesMySongs;

    }

    /**
     * @author Thomas
     * Methode qui va charger les infos necessaires pour le server dans une List
     */

    private List<Object> collectMyInfo() {

        List<Object> myCollectedInfo = new ArrayList<>();
        myCollectedInfo.add(findIpAddress());
        myCollectedInfo.add(portClientClient);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());
        myCollectedInfo.add(myRole);

        return myCollectedInfo;
    }


    /**
     * @author Thomas
     * Methode regroupant l ensemble des methodes et demarches necessaires pour la demande
     * aupres d un autre client d un audio jusqu a sa reception et a son ecoute
     */

    private void askClientForAnAudio(){

        List<Object> clientMusicInfo =  readMessageConvertToList(exchangeSocket);
        InetAddress ipOfOtherClient = (InetAddress) clientMusicInfo.get(0);
        int port = (Integer) clientMusicInfo.get(1);

        Socket comSocket = null;

        try {
            comSocket = new Socket(ipOfOtherClient, port);
            System.out.println("Client connected to other Client : socket" + ipOfOtherClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendSomethingToSomeone(comSocket,clientMusicInfo);
        connectToOtherClient(ipOfOtherClient, port);

    }

    /**
     * author Thomas
     * Methode regroupant l ensemble des methodes et des demarches necessaires
     * pour la reception d un requete d un autre client pour un audio jusqu a sa diffusion
     */

    private void giveClientAnAudio(){

        List<Object>request = receiveClientRequest();
        transferAudio(request);

    }

    /**
     * @author Thomas
     * Methode servant a effectuer la demonstration de maniere sequentielle.
     * Le premier client qu on utilise doit OBLIGATOIREMENT se definir comme nr. 1 Listener Client
     * Le server a ete defini dans ce sens la.
     */

    public void whoAreYou(){

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
                        myRole=1;

                        start();
                        sendSomethingToSomeone(exchangeSocket, musicChoice());
                        askClientForAnAudio();
                        break;

            case 2 :    System.out.println("You have selected Client Broadcaster");
                        System.out.println("******************************************");
                        myMusicRepertory="C://temp//AudioStream2//myMusic";
                        portClientClient = 25250;
                        myRole=2;

                        start();
                        giveClientAnAudio();
                        break;

            default:    System.out.println("Sorry, something went wrong please restart");
                        System.out.println("******************************************");

        }

    }

    /**
     * @author Thomas
     * methode qui permet de recuperer un choix d'un client dans la console
     */

    private int myChoice() {

        int myChoice = 0;
        boolean check = false;

        do {
            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter the number of your choice ");

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
        while (!check);

        return myChoice;
    }

}