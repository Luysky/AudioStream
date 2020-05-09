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

    private InetAddress serverAddress;
    private InetAddress inetAddress = null;

    //portServer est fixe car connu par le programme
    protected int portServer = 17257;

    //Client récupère le Socket qui est distribué à lui par le Serveur
    protected Socket clientComServerSocket;

    private ServerSocket clientlisteningSocket;
    protected int portClientServer;
    protected int portClientClient;
    private int ClientNumber = 1;
    private Socket clientSocket;
    private Socket socketForOtherClient;

    //portClient est fixe également
    // mais vu que l'on travaille sur la meme machine il faut un port different pour chaque client
    //Le port est distribué par le Server automatiquement


    protected String myMusicRepertory = "C://temp//AudioStream//myMusic";
    protected List<String> myMusic= new ArrayList<>();
    protected List<String> myInfo = new ArrayList<>();
    protected String clientName ="default";
    private String questionOne = "Veuillez donner votre nom";

    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss");
    String dateNow = formatter.format(currentDate.getTime());
    
    BufferedReader buffInForClient;
    PrintWriter printForOtherClient;
    PrintWriter writeForOtherClient;
    BufferedReader buffToWriteMessage;


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

        startClientLogger();

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

        //return localAddress.getHostAddress();
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

            //impression de la liste
            //myMusic.forEach(System.out::println);

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
        //A ENVOYER portClientClient
        //myCollectedInfo.add(portClientServer);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());

        return myCollectedInfo;
    }

    public void startClientSockets() throws IOException, InterruptedException {

        /**
         * @author_Thomas_et_Marina
         * Methode servant a initier les Socket de Server et d'echange pour les clients
         */

        System.out.println("Client name:  " + clientName);

        try{
            serverAddress = findIpAddress();
            System.out.println("Get the address of the server : "+ serverAddress);
            ClientLogger.info("The address of the server : " + serverAddress);

            clientSocket = new Socket(serverAddress, 17257);
            System.out.println("I got connection to " + serverAddress);
            ClientLogger.info("We got connection to " + serverAddress);


            //On choisi un port client aléatoirement

            portClientServer = clientSocket.getLocalPort();
            System.out.println("clientPort " + portClientServer);
            ClientLogger.info("client port " + portClientServer);

            /*
            // now we wait for something ??
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

             */

            OutputStream outputStream = clientSocket.getOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(collectMyInfo());





        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("server connection error, dying.....");
        }catch (NullPointerException e){
            System.out.println("Connection interrupted with the server");
        }


    }

    public void startClientListeningSocket(){
        try {

            clientlisteningSocket = new ServerSocket(portClientServer, 10, inetAddress);

            //System.out.println("Default Timeout :" + listeningSocket.getSoTimeout());
            //System.out.println("Used IpAddress :" + listeningSocket.getInetAddress());
            System.out.println("Listening to Port :" + clientlisteningSocket.getLocalPort());
            ClientLogger.info("Client listens to Port :" + clientlisteningSocket.getLocalPort());
            System.out.println();

            while (true) {
                socketForOtherClient = clientlisteningSocket.accept();

                System.out.println("******************************************");

                System.out.println("I am listening ");
                Thread acceptClientThread = new Thread(new AcceptClient(socketForOtherClient, ClientNumber));
                ClientNumber++;
                acceptClientThread.start();
                //sendInfoFromClient();
            }

        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        }


    }

    public void receivedInfo(){

        /**
         * @author Thomas
         * Methode qui va permettre de recuperer des informations entrantes venant du server.
         * A MODIFIER POUR TRANSFORMATION UTILISATION DE LA LISTE D'OBJET
         */

        System.out.println("A client is connected");


        InputStream inputStream = null;
        try {
            inputStream = clientComServerSocket.getInputStream();
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

        List<Object> incomingRocket = null;
        try {
            incomingRocket = (List<Object>) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
            startClientSockets();
        } catch (IOException e) {
            ClientLogger.severe("IOException " + e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            ClientLogger.severe("InterruptedException " + e.toString());
            e.printStackTrace();
        }

        startClientListeningSocket();

        sendMessageToOtherClient();

        getMessageFromOtherClient();

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
}
