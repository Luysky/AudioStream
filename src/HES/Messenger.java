package HES;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Messenger implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scan;
    private String line = "";
    private String answer = "";




    public Messenger(Socket socket) throws IOException {
        this.socket=socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        scan = new Scanner(System.in);

    }


    @Override
    public synchronized void run() {

        try{

            while (line.compareTo("QUIT") != 0) {

                line=in.readLine();
                System.out.println(" Message from Client " +  line);

                line = scan.nextLine();

                out.print(line + "\r\n");

                out.flush();
                System.out.println("Response to Client  :  "+line);

            }

            System.out.println( "CONNECTION ENDED ");

            socket.close();

        } catch (IOException ex) {

            System.out.println(": IO Error on socket " + ex);

            return;

        }

    }
}
