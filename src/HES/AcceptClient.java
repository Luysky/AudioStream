package HES;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptClient implements Runnable {

    protected Socket clientSocketOnServer;
    private int clientNumber = 1;

    //Constructor
    public AcceptClient(Socket s){
        clientSocketOnServer = s;
    }

    //overwrite the thread run()
    public void run() {

        try {
               // clientSocketOnServer = socketServer.accept(); // A client wants to connect, we accept him
                System.out.println("Client Nr "+clientNumber+ " is connected");
                System.out.println("Port " + clientSocketOnServer.getPort());
                Thread.sleep(3000);
                clientNumber++;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Socket getClientSocketOnServer(){
        return clientSocketOnServer;
    }
}
