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
     * @author Thomas/Marina
     * Classe mère pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
     */
    protected final static Logger ClientLogger = Logger.getLogger("ClientLog");

    protected InetAddress localAddress = null;
    private String interfaceName = "wlan1"; // ?? à determiner

    private String serverAddress = "192.168.0.15"; // à adapter!!!!!!!!!!!!!!!!!!
    private InetAddress serverIP;

    //portServer est fixe car connu par le programme
    protected int serverPort = 17257;

    //Client récupère le Socket qui est distribué à lui par le Serveur


    private ServerSocket clientlisteningSocket;
    private Socket exchangeSocket;

    private Socket socketForOtherClient;
    private Socket exchangeSocketForOtherClient;
    static ArrayList<ClientToClient> otherClientsList = new ArrayList<>();

    protected int portClientServer;
    protected int portClientClient;

    private int ClientNumber = 1;
    private String incomingMessage = "";
    private InputStream inputStream = null;

    protected String myMusicRepertory = "C://temp//AudioStream//myMusic";
    protected List<String> myMusic = new ArrayList<>();
    protected List<String> myInfo = new ArrayList<>();
    protected String clientName = "default";
    private String questionOne = "Veuillez donner votre nom";

    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = formatter.format(currentDate.getTime());

    private BufferedReader buffInForClient;
    private BufferedReader buffToWriteMessage;
    private PrintWriter printForOtherClient;
    private PrintWriter writeForOtherClient;

    private int selectedMusic = 0;

    public ClientAlpha() { }

    protected void start (){

        clientName = myChoice(questionOne);
        startClientLogger();
        try {
            startClientSockets();
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            ClientLogger.severe("InterruptedException " + e.toString());
            e.printStackTrace();
        }



        sendSomethingToSomeone(exchangeSocket, collectMyInfo());
        readIncomingMessage(exchangeSocket);

 /*
        Thread sendToServer = new Thread(new Runnable() {
            @Override
            public void run() {
                sendSomethingToSomeone(exchangeSocket, collectMyInfo());

            }
        });


        Thread readFromServer = new Thread(new Runnable() {
            @Override
            public void run() {

                readIncomingMessage(exchangeSocket);
            }
        });

        sendToServer.start();
        readFromServer.start();

        if(sendToServer.isAlive()){
            try{
                sendToServer.join();
            }catch(InterruptedException e){
                ClientLogger.severe("InterruptedException in Thread sending to Server" + e.toString());
            }
        }
/*
        if(readFromServer.isAlive()){
            try{
                readFromServer.join();
            }catch(InterruptedException e){
                ClientLogger.severe("InterruptedException in Thread reading from Server" + e.toString());
            }
        } */


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private String myChoice(String question) {

        /**
         * @Thomas
         * methode qui permet de recuperer un choix d'un client dans la console
         */

        Scanner scan = new Scanner(System.in);

        String choice = "default";

        System.out.println(question);
        choice = scan.nextLine();


        return choice;
    }

    public int musicChoice() {


        Scanner scan = new Scanner(System.in);

        int myChoice = 0;

        System.out.println("Veuillez saisir le numero de l'audio que vous désirez : ");

        try {

            myChoice = scan.nextInt();


        } catch (InputMismatchException e) {
            System.out.println("Uniquement un chiffre !");
            scan.next();

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Chiffre indiponible !");
            scan.next();
        }

        return myChoice;
    }

    public void startClientLogger() {

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
        ClientLogger.info("********************** program starts ******************");

    }

    public void startClientSockets() throws IOException, InterruptedException {

        /**
         * @author_Thomas_et_Marina
         * Methode servant a initier les Socket de Server et d'echange pour les clients
         */

        System.out.println("Client name:  " + clientName);

        try {

            serverIP = InetAddress.getByName(serverAddress);
            ClientLogger.info("Get the address of the server : "+ serverIP);

            //exchangeSocket = new Socket(serverIP, serverPort);
            exchangeSocket = new Socket(findIpAddress(), serverPort);

        } catch (NullPointerException e) {
            System.out.println("Connection interrupted with the server");
        }

    }

    public void sendSomethingToSomeone(Socket exchangeSocket, Object object) {

        /**
         * @author Thomas
         * methode servant a envoyer un objet a un autre client
         * il faut donner en parametre d'entree son ip, son port et notre message en objet
         */

        try {

            ClientLogger.info("I am connected to " + exchangeSocket.getPort());

            OutputStream outputStream = exchangeSocket.getOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(object);

            ClientLogger.info("Message envoyé" + object);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listeningToClients() {

        /**
         * @author Thomas/Marina
         * Methode qui sert a recevoir depuis le server le menu des audios
         * c'est egalement ici que l'on fait le choix de l'audio que l'on veut
         */

        boolean useOfMethode = true;
        int cptloop = 0;

        try {

            clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
            ClientLogger.info("Default Timeout :" + clientlisteningSocket.getSoTimeout());
            ClientLogger.info("Client listening to other clients, port :" + clientlisteningSocket.getLocalPort());
            System.out.println();

           // while (useOfMethode) {
                socketForOtherClient = clientlisteningSocket.accept();
                ClientLogger.info("Socket for Other Client created " + socketForOtherClient);

                System.out.println("*************To Change!!!!!!!*****************");

                File myFile = new File("C://temp//AudioStream2//myMusic//audio.wav");
                byte[] mybytearray = new byte[(int)myFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray, 0, mybytearray.length);


            //    BufferedInputStream is = new BufferedInputStream(socketForOtherClient.getInputStream());
                OutputStream os = socketForOtherClient.getOutputStream();
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();

           //     Thread acceptOtherClientThread = new Thread(socketForClient);
           //     otherClientsList.add(socketForClient);
           //     ClientLogger.info("Client " + cptloop + " added to the list " + otherClientsList);
           //     acceptOtherClientThread.start();

               /* cptloop++;

                if (cptloop == 1) {
                    useOfMethode = false;
                } */



        } catch (IOException e) {
            e.printStackTrace();
            ClientLogger.severe("IO exception " + e.toString());
        }

    }

    public void connectToOtherClient(InetAddress ipClient, int port){

        System.out.println("IP " + ipClient + " port " + port);
        try {
            exchangeSocketForOtherClient = new Socket(ipClient, port);
            ClientLogger.info("Client connected to other Client : socket" + exchangeSocketForOtherClient);
        } catch (IOException e) {
            ClientLogger.severe("IOException while connection to Other Client" + e.toString());
            e.printStackTrace();
        }

        try {
            BufferedInputStream bis = new BufferedInputStream(exchangeSocketForOtherClient.getInputStream());
            System.out.println("Client start getting InputStream");


            try {
                System.out.println("Try to put bis into Player");
                AudioPlayer player = new AudioPlayer(bis);
                System.out.println("Try  to play");
                player.play();
                try {
                    Thread.sleep(player.clip.getMicrosecondLength());
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

    public void readIncomingMessage(Socket socket) {

        /**
         * @author Thomas/Marina
         * Methode qui va permettre de recuperer des informations entrantes venant du server.
         * A MODIFIER POUR TRANSFORMATION UTILISATION DE LA LISTE D'OBJET
         */

        System.out.println("Incoming message");

        try {
            inputStream = socket.getInputStream();
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

        try {
            incomingMessage = (String) objectInputStream.readObject();

            System.out.println();
            System.out.println("Message from :" + socket.getPort());
            System.out.println(incomingMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    protected InetAddress findIpAddress() {
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

    protected String findInterface() throws SocketException {

        /**
         * @author Thomas
         * Methode qui va detecter automatiquement l'interface wlan utilisee par le client
         */

        String myInterface = "";

        Enumeration<NetworkInterface> allNi;

        //getNetworkInterfaces avoir toutes les interfaces de notre machine
        allNi = NetworkInterface.getNetworkInterfaces();

        //itérateur pour chercher le nouvel élément

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

    protected List<Integer> searchSizesMySongs() {

        /**
         * @Thomas
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

    protected List<Object> collectMyInfo() {

        /**
         * @author Thomas
         * Methode qui va charger les infos necessaires pour le server dans une List
         * LA FUSEE
         */

        List<Object> myCollectedInfo = new ArrayList<>();
        myCollectedInfo.add(findIpAddress());
        myCollectedInfo.add(portClientClient);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());

        return myCollectedInfo;
    }

    public void getMessageFromOtherClient() {

        String messageFromOtherClient = "";

        //get an input stream from the socket to read data from the server
        try {
            buffInForClient = new BufferedReader(new InputStreamReader(socketForOtherClient.getInputStream()));
            printForOtherClient = new PrintWriter(socketForOtherClient.getOutputStream());
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }

        //listen to the input from the socket
        //exit when the order quit is given
        while (true) {
            //Read a line in the buffer, wait until something arrive remove the last cr
            System.out.println("wait message from server...");
            ClientLogger.info("wait message from server...");

            try {
                messageFromOtherClient = buffInForClient.readLine().trim();
            } catch (IOException e) {
                ClientLogger.severe("IOException " + e.toString());
                e.printStackTrace();
            }
            printForOtherClient.println(messageFromOtherClient);
            printForOtherClient.flush();

            //display message received by the server
            System.out.println("\nMessage received from server:\n" + messageFromOtherClient);
            ClientLogger.info("\nMessage received from server:\n" + messageFromOtherClient);

            //if quit then exit the loop
            if (messageFromOtherClient.equals("quit")) {
                System.out.println("\nquit sent from server...");
                ClientLogger.info("\nquit sent from server...");
                break;
            }
        }

    }

    public void sendMessageToOtherClient() {

        //open the output data stream
        try {
            writeForOtherClient = new PrintWriter(socketForOtherClient.getOutputStream());
            buffToWriteMessage = new BufferedReader(new InputStreamReader(socketForOtherClient.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create the scanner to accept data from the console
        Scanner sc = new Scanner(System.in);

        String message_distant = "";

        //loop on the client connection in/out
        while (true) {
            //Send a message to the client
            System.out.println("Send message to client: ");
            ClientLogger.info("Send message to client: ");
            String message = sc.nextLine();
            writeForOtherClient.println(message);
            ClientLogger.info(message);
            writeForOtherClient.flush();

            //Read a line from the input buffer, remove the last cr
            try {
                message_distant = buffToWriteMessage.readLine().trim();
            } catch (IOException e) {
                ClientLogger.severe("IOException " + e.toString());
                e.printStackTrace();
            }

            //Display the message sent by the client
            System.out.println("\nReceive message from client:\n" + message_distant);
            ClientLogger.info("Receive message from client:" + message_distant);

            //if the order is quit then exit from the loop
            if (message_distant.equals("quit")) {
                System.out.println("\nReceived the quit message....");
                ClientLogger.info("Received the quit message....");
                break;
            }
        }
    }


}