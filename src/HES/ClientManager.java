package HES;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String gotMessage;
    private int clientNumber = 0;
    private ArrayList<ClientManager> allClients;
    private ExecutorService poolForMusic = Executors.newFixedThreadPool(4);
    private PlayerForAllClients player;



    private Scanner scan;
    private String line;


    public ClientManager(Socket s, ArrayList<ClientManager> clients) throws IOException {

        this.clientSocket=s;
        this.allClients = clients;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        scan = new Scanner(System.in);

    }

    public void run(){

            try{

                while(true) {
                    clientNumber = clientSocket.getPort();
                    gotMessage = in.readLine();
                    System.out.println(gotMessage);

                    if(gotMessage.contains("name")){
                        out.println("Client " + clientNumber);
                    }else if(gotMessage.startsWith("play music")){
                        System.out.println("I got a request to play music to all");
                        playMusicToAll();
                    }else if(gotMessage.matches(".*\\d.*")){
                        System.out.println("I got request to play a song");
                            String request = gotMessage.toString();
                            int nbrSong = Integer.parseInt(extractNumber(request));
                            System.out.println("Requested song " + nbrSong);
                            player = new PlayerForAllClients(clientSocket, nbrSong);
                            poolForMusic.execute(player);
                           // playMusic(nbrSong);
                    }else{

                        line = scan.nextLine();

                        out.print(line + "\r\n");

                        out.flush();
                        System.out.println("Response to Client  :  "+line);
                    }
                }

            }catch(IOException e){
                System.err.println("IOException while reading an incoming message" + e);
            }finally {
                out.close();
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


    }

    public void playMusicToAll(){

        System.out.println(allClients);
        for(ClientManager client : allClients){
            System.out.println("Client " + client);
            player = new PlayerForAllClients(client.clientSocket, 2);
            client.poolForMusic.execute(player);
            System.out.println("Played for Client " + client);
        }
    }


    public String extractNumber(String str) {

        if(str == null || str.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for(char c : str.toCharArray()){
            if(Character.isDigit(c)){
                sb.append(c);
                found = true;
            } else if(found){
                // If we already found a digit before and this char is not a digit, stop looping
                break;
            }
        }

        return sb.toString();
    }

}
