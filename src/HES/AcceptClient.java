package HES;

import java.net.Socket;


/**
 * @author Marina
 * Classe servant a l'enregistrement de nouveaux clients.
 */

public class AcceptClient implements Runnable {


    protected Socket clientSocketOnServer;

    public AcceptClient(Socket s){
        clientSocketOnServer = s;
    }


    public void run() {

        try {
                System.out.println();
                System.out.println("******************************************");
                System.out.println("A new client is connected");
                System.out.println("Client Port : " + clientSocketOnServer.getPort());
                Thread.sleep(3000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
