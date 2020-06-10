package HES;

import java.io.*;
import java.net.Socket;

public class PlayerForAllClients  implements Runnable{

    private String firstSong ="C://temp//AudioStream2//myMusic//Apocalypse-Now.wav";
    private String secondSong="C://temp//AudioStream2//myMusic//audio.wav";
    private String thirdSong = "C://temp//AudioStream2//myMusic//LongRoad.wav";
    private String path;

    private BufferedInputStream bis;
    private OutputStream os;
    private Socket clientSocket;
    private int nbrMusic;

    public PlayerForAllClients(Socket s, int number){
        this.clientSocket=s;
        this.nbrMusic=number;
    }
    @Override
    public void run() {
        try {
            playMusic(nbrMusic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void playMusic(int number) throws IOException {

        System.out.println("I try to start PlayerForAll " + number);

        switch(number){

            case 1: path = firstSong;
                break;
            case 2: path = secondSong;
                break;
            case 3: path = thirdSong;
                break;
            default: System.out.println("Sorry, there isn't such song");
                System.out.println("******************************************");
        }

        File myFile = new File(path);
        byte[] mybytearray = new byte[(int)myFile.length()];
        System.out.println("The file's size " + mybytearray.length);
        bis = new BufferedInputStream(new FileInputStream(myFile));
        bis.read(mybytearray, 0, mybytearray.length);

        os = clientSocket.getOutputStream();
        os.write(mybytearray, 0, mybytearray.length);
        os.flush();
        System.out.println("The song " + path + " was send to Client " + clientSocket.getPort());
    }
}
