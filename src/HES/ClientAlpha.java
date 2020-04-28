package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientAlpha implements Runnable {

    /**
     * @author Thomas/Marina
     * Classe mère pour les clients. L'ensemble des méthodes et des interactions Server/Client sont réglés ici.
     */

    //Avec les méthodes plus besoin de nos adresses en dur.
    //ipAddress Thomas
    //protected String ipAddress = "192.168.56.1";

    //ipAddress Marina
    //protected String ipAddress = "192.168.0.15";

    //portServer est fixe car connu par le programme
    protected int portServer = 17257;

    //portClient est fixe également
    // mais vu que l'on travaille sur la meme machine il faut un port different pour chaque client
    protected int portClient = 00000;


    protected List<String> myMusic= new ArrayList<>();
    protected List<String> myInfo = new ArrayList<>();

    protected String clientName = "unkwnow";

    public ClientAlpha() {



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


    protected String findIpAddress(){

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

                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return localAddress.getHostAddress();
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

        try (Stream<Path> walk = Files.walk(Paths.get("C://temp//AudioStream//myMusic"))) {

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
        myCollectedInfo.add(portClient);
        myCollectedInfo.add(searchMyMusic());
        myCollectedInfo.add(searchSizesMySongs());


        return myCollectedInfo;

    }

    public void startClientSockets() throws IOException {

        /**
         * @author Thomas
         * Methode servant a initier les Socket de Server et d'echange pour les clients
         */

        System.out.println(clientName);
        ServerSocket listeningSocket = new ServerSocket(portClient);

            //A determiner l'utilite de ce Thread
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        Socket exchangeSocket = new Socket(findIpAddress(), portServer);
        System.out.println("I am connected");

        OutputStream outputStream = exchangeSocket.getOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(collectMyInfo());



        System.out.println("Closing socket and terminating program.");

        exchangeSocket.close();


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
