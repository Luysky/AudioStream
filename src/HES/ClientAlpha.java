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

public class ClientAlpha implements Runnable {

    /**
     * @author Thomas/Marina
     * Classe mère pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
     */


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

    //portClient est fixe également
    // mais vu que l'on travaille sur la meme machine il faut un port different pour chaque client
    //Le port est distribué par le Server automatiquement


    protected String myMusicRepertory = "C://temp//AudioStream//myMusic";
    protected List<String> myMusic= new ArrayList<>();
    protected List<String> myInfo = new ArrayList<>();
    protected String clientName ="default";
    private String questionOne = "Veuillez donner votre nom";


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
         * @author Thomas
         * methode qui va demarrer l'activite du client
         * pour l'instant pas essentiel startClientSockets pourrait suffire.
         * A ete creer afin d'integrer au besoin une interaction avec l'utilisateur.
         * (Genre demande du nom utilisateur ou chemin d'acces repertoire audio)
         */


        try {
            startClientSockets();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
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

            Socket clientSocket = new Socket(serverAddress, 17257);
            System.out.println("I got connection to " + serverAddress);


            //On choisi un port client aléatoirement

            portClientServer = clientSocket.getLocalPort();
            System.out.println("clientPort " + portClientServer);

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



            try {

                clientlisteningSocket = new ServerSocket(portClientServer, 10, inetAddress);

                //System.out.println("Default Timeout :" + listeningSocket.getSoTimeout());
                //System.out.println("Used IpAddress :" + listeningSocket.getInetAddress());
                System.out.println("Listening to Port :" + clientlisteningSocket.getLocalPort());
                System.out.println();

                while (true) {
                    clientSocket = clientlisteningSocket.accept();

                    System.out.println("******************************************");

                    System.out.println("I am listening ");
                    Thread acceptClientThread = new Thread(new AcceptClient(clientSocket, ClientNumber));
                    ClientNumber++;
                    acceptClientThread.start();
                    //sendInfoFromClient();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }



        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("server connection error, dying.....");
        }catch (NullPointerException e){
            System.out.println("Connection interrupted with the server");
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

}
