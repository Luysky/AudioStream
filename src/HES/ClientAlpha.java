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

public class ClientAlpha implements Runnable {

    /**
     * @author Thomas/Marina
     * Classe mère pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
     */
    private final static Logger ClientLogger = Logger.getLogger("ClientLog");

    private InetAddress localAddress = null;
    private String interfaceName = "wlan1"; // ?? à determiner

    private InetAddress serverAddress;
    private InetAddress inetAddress = null;

    //portServer est fixe car connu par le programme
    protected int portServer = 17257;

    //Client récupère le Socket qui est distribué à lui par le Serveur


    private ServerSocket clientlisteningSocket;
    private Socket clientExchangeSocket;

    protected Socket clientComServerSocket;
    private Socket socketForOtherClient;

    protected int portClientServer;
    protected int portClientClient;

    private int ClientNumber = 1;


    //portClient est fixe également
    // mais vu que l'on travaille sur la meme machine il faut un port different pour chaque client
    //Le port est distribué par le Server automatiquement


    protected String myMusicRepertory = "C://temp//AudioStream//myMusic";
    protected List<String> myMusic= new ArrayList<>();
    protected List<String> myInfo = new ArrayList<>();
    protected String clientName ="default";
    private String questionOne = "Veuillez donner votre nom";

    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    private String dateNow = formatter.format(currentDate.getTime());
    
    private BufferedReader buffInForClient;
    private BufferedReader buffToWriteMessage;
    private PrintWriter printForOtherClient;
    private PrintWriter writeForOtherClient;


    public ClientAlpha() {


        clientName=myChoice(questionOne);


    }


    private String myChoice (String question){

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

    protected void startClient(){

        /**
         * @author Thomas et Marina
         * methode qui va demarrer l'activite du client
         * pour l'instant pas essentiel startClientSockets pourrait suffire.
         * A ete creer afin d'integrer au besoin une interaction avec l'utilisateur.
         * (Genre demande du nom utilisateur ou chemin d'acces repertoire audio)
         */

        findConnectionInfo();
        startClientLogger();
        listening();

    }

    public void startClientLogger(){

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

        try {

            //C'est l'envoie de la fusée
            startClientSockets();


        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            ClientLogger.severe("InterruptedException " + e.toString());
            e.printStackTrace();
        }

        System.out.println("Port Server : "+portServer);
        System.out.println("Port Clientclient "+portClientClient);
        System.out.println("Port ClientServer "+portClientServer);

        //startClientListeningSocket();




        //sendMessageToOtherClient();

        //getMessageFromOtherClient();

    }

    public void startClientSockets() throws IOException, InterruptedException {

        /**
         * @author_Thomas_et_Marina
         * Methode servant a initier les Socket de Server et d'echange pour les clients
         */

        System.out.println("Client name:  " + clientName);

        try{
            serverAddress = findIpAddress();
            //System.out.println("Get the address of the server : "+ serverAddress);
            ClientLogger.info("The address of the server : " + serverAddress);

            clientExchangeSocket = new Socket(serverAddress, 17257);
            //System.out.println("I got connection to " + serverAddress);
            ClientLogger.info("We got connection to " + serverAddress);


            //On reçoit un port client aléatoirement

            portClientServer = clientExchangeSocket.getLocalPort();
            //System.out.println("clientPort " + portClientServer);
            ClientLogger.info("client port " + portClientServer);

            /*
            // now we wait for something ??
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

             */

            OutputStream outputStream = clientExchangeSocket.getOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(collectMyInfo());

            //clientExchangeSocket.close();




        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("server connection error, dying.....");
        }catch (NullPointerException e){
            System.out.println("Connection interrupted with the server");
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
            System.out.println("Connection Timed out");
            ClientLogger.severe("Connection Timed out");
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

    public void listening(){

        try {

            clientlisteningSocket = new ServerSocket(portClientClient, 10, localAddress);
            //ClientLogger.info("Default Timeout :" + clientlisteningSocket.getSoTimeout());
            ClientLogger.info("Client listening to port :" + clientlisteningSocket.getLocalPort());
            System.out.println();

            while (true) {

                clientExchangeSocket = clientlisteningSocket.accept();

                System.out.println("******************************************");

                System.out.println("I am listening ");
                Thread acceptClientThread = new Thread(new AcceptClient(clientExchangeSocket, ClientNumber));
                ClientNumber++;
                acceptClientThread.start();

                receivedInfo();



                //receiveInfoFromClient();
                //retreivedIpFromClient(clientsList,0);
                //System.out.println(musicString(giveMusicList(0)));

            }

        } catch (IOException e) {
            e.printStackTrace();
            ClientLogger.severe("IO exception " + e.toString());
        }

    }

    public void receivedInfo(){

        /**
         * @author Thomas
         * Methode qui va permettre de recuperer des informations entrantes venant du server.
         * A MODIFIER POUR TRANSFORMATION UTILISATION DE LA LISTE D'OBJET
         */

        System.out.println("Incoming message");


        InputStream inputStream = null;
        try {
            inputStream = clientExchangeSocket.getInputStream();
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


        // Recuperation des informations de la fusee venant du Server

        String incomingMessage = null;



        try {
            incomingMessage = (String) objectInputStream.readObject();

            System.out.println();
            System.out.println("Menu Audio :");
            System.out.println(incomingMessage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }




    //protected String findIpAddress(){
    protected InetAddress findIpAddress(){
        /**
         * @author Thomas
         * Methode qui va servir à determiner automatiquement l'adresse ip du client et son interface
         */

        InetAddress localAddress=null;
        NetworkInterface ni;
        try {
            ni = NetworkInterface.getByName(findInterface());
            Enumeration<InetAddress> LocalAddress = ni.getInetAddresses();

            while (LocalAddress.hasMoreElements()) {
                InetAddress ia = LocalAddress.nextElement();
                if (!(ia instanceof Inet6Address) && !(ia.isLoopbackAddress())) {
                    localAddress = ia;

                    inetAddress = ia;
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return localAddress;
    }

    protected String findInterface () throws SocketException {

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


            if (nix.isUp()==true) {

                if(nix.getName().contains("w")){
                    myInterface=nix.getName();
                }
            }
        }
        return myInterface;
    }


    protected List<String> searchMyMusic (){

        /**
         * @author Thomas
         * Methode qui va repertorier tous les fichiers wav se trouvant dans le repertoire du client
         */

        List<String> myMusic=new ArrayList<>();

        try (Stream<Path> walk = Files.walk(Paths.get(myMusicRepertory))) {

            myMusic = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".wav")).collect(Collectors.toList());


        } catch (IOException e) {
            e.printStackTrace();
        }

        return myMusic;
    }


    protected List<Integer> searchSizesMySongs(){

        /**
         * @Thomas
         * Methode qui va chercher la taille necessaire pour les array de byte
         * de chaque chanson du client
         */

        List<Integer>sizesMySongs = new ArrayList<>();
        int size = 0;

        myMusic=searchMyMusic();

        for(int i =0; i<myMusic.size();i++){

            File myFile = new File(myMusic.get(i));
            byte[] mybytearray = new byte[(int)myFile.length()];

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
        myCollectedInfo.add(clientName);
        myCollectedInfo.add(findIpAddress());
        myCollectedInfo.add(portClientClient);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());

        return myCollectedInfo;
    }



    public void startClientListeningSocket(){
        try {

            clientlisteningSocket = new ServerSocket(portClientServer, 10, inetAddress);

            //System.out.println("Default Timeout :" + listeningSocket.getSoTimeout());
            //System.out.println("Used IpAddress :" + listeningSocket.getInetAddress());
            //System.out.println("Listening to Port :" + clientlisteningSocket.getLocalPort());
            ClientLogger.info("Client listens to Port :" + clientlisteningSocket.getLocalPort());
            System.out.println();

            while (true) {
                socketForOtherClient = clientlisteningSocket.accept();

                System.out.println("******************************************");

                System.out.println("I am listening ");
                Thread acceptClientThread = new Thread(new AcceptClient(socketForOtherClient, ClientNumber));
                acceptClientThread.start();
                listening();


            }

        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }


    }




    public void listIterator(List<String> c) {

        /**
         * @author Thomas
         * Methode permettant d'afficher une liste de String avec numerotation devant chaque element
         */

        int cpt = 1;

        Iterator<String> i = c.iterator();

        while (i.hasNext()) {

            String l = i.next();
            System.out.println(cpt + ". " + l);
            cpt++;
        }

        System.out.println();

    }

    public void listIteratorInt(List<Integer> c) {

        /**
         * @author Thomas
         * Methode permettant d'afficher une liste de int avec numerotation devant chaque element
         */

        int cpt = 1;

        Iterator<Integer> i = c.iterator();

        while (i.hasNext()) {

            int l = i.next();
            System.out.println(cpt + ". " + l);
            cpt++;
        }

        System.out.println();

    }


    
    public void getMessageFromOtherClient(){
        
        String messageFromOtherClient = "";

        //get an input stream from the socket to read data from the server
        try {
            buffInForClient = new BufferedReader (new InputStreamReader (socketForOtherClient.getInputStream()));
            printForOtherClient = new PrintWriter(socketForOtherClient.getOutputStream());
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }

        //listen to the input from the socket
        //exit when the order quit is given
        while(true)
        {
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
            System.out.println("\nMessage received from server:\n"+messageFromOtherClient);
            ClientLogger.info("\nMessage received from server:\n"+messageFromOtherClient);

            //if quit then exit the loop
            if (messageFromOtherClient.equals("quit"))
            {
                System.out.println("\nquit sent from server...");
                ClientLogger.info("\nquit sent from server...");
                break;
            }
        }

    }

    public void sendMessageToOtherClient(){

        //open the output data stream
        try {
            writeForOtherClient = new PrintWriter(socketForOtherClient.getOutputStream());
            buffToWriteMessage = new BufferedReader (new InputStreamReader (socketForOtherClient.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create the scanner to accept data from the console
        Scanner sc = new Scanner(System.in);

        String message_distant = "";

        //loop on the client connection in/out
        while(true)
        {
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
            System.out.println("\nReceive message from client:\n"+message_distant);
            ClientLogger.info("Receive message from client:"+message_distant);

            //if the order is quit then exit from the loop
            if (message_distant.equals("quit"))
            {
                System.out.println("\nReceived the quit message....");
                ClientLogger.info("Received the quit message....");
                break;
            }
        }
    }

    //deprecated - a supprimer lorsqu'on aura integrer le syteme audio ailleurs
    @Override
    public void run() {

        System.out.println("Client online");


        try{

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Socket exchangeSocket = new Socket(findIpAddress(), portServer);
            System.out.println("I am connected");

            InputStream is = new BufferedInputStream(exchangeSocket.getInputStream());

            try {
                AudioPlayer player = new AudioPlayer(is);
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


            /*FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesRead = is.read(mybytearray, 0, mybytearray.length);
            bos.write(mybytearray, 0, mybytearray.length);

            bos.close(); */


            is.close();
            exchangeSocket.close();


        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
