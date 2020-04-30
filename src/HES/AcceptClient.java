package HES;

import java.io.IOException;
import java.net.Socket;

public class AcceptClient implements Runnable {

    protected Socket clientSocketOnServer;
    private int clientNumber;

    //Constructor
    public AcceptClient (Socket clientSocketOnServer, int clientNo)
    {
        this.clientSocketOnServer = clientSocketOnServer;
        this.clientNumber = clientNo;

    }
    @Override
    public void run() {

        try {
            System.out.println("Client Nr "+clientNumber+ " is connected");
            System.out.println("Socket is available for connection"+ clientSocketOnServer);
            Thread.sleep(30000); // Ajouter le temps pour que le Client peut transmettre info
           // clientSocketOnServer.close();

           // System.out.println("end of connection to the client " + clientNumber);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Socket getClientSocketOnServer(){
        return clientSocketOnServer;
    }

}