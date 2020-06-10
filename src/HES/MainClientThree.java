package HES;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainClientThree {

    // Client part
    private InetAddress address;
    private Socket s1 = null;
    private String line = null;
    private BufferedReader br = null;
    private BufferedReader is = null;
    private PrintWriter os = null;
    private InputStream inputForMusic;
    private Scanner scan;



    public void connectToServer() {

        try {
            address = InetAddress.getLocalHost();
            s1 = new Socket(address, 25250); // You can use static final constant PORT_NUM

        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("IO Exception");
        }
        talkToOthers(s1);

    }
    public void talkToOthers(Socket s) {

        br = new BufferedReader(new InputStreamReader(System.in));
        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream(), true);
            inputForMusic = new BufferedInputStream(s.getInputStream());
        }catch(IOException e){
            System.out.println("IOException while talking" + e);
        }
        System.out.println("Client Address : " + s.getInetAddress() + ", port " + s.getPort());
        System.out.println("Enter Data to echo Server ( Enter QUIT to end):");


        String response = null;
        try {
            line = br.readLine();

            while (line.compareTo("QUIT") != 0) {

                if(line.matches(".*\\d.*")){
                    os.println(line);
                    os.flush();
                    listenToMusic(inputForMusic);
                }else if (line.startsWith("play music")){
                    os.println(line);
                    os.flush();
                    System.out.println("I asked to play music to all ");
                    listenToMusic(inputForMusic);
                }else {
                    os.println(line);
                    os.flush();
                    response = is.readLine();
                    System.out.println("Server Response : " + response);
                    line = br.readLine();
                }
            }

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
                System.out.println("Listening music");
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


    public static void main(String[] args) {

       MainClientThree client3 = new MainClientThree();
       client3.connectToServer();


    }
}
