package HES;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketForClient implements Runnable {

    private ServerSocket serverSocket;
    private Socket socket;
    InetAddress serverAddress;

    public SocketForClient(ServerSocket ss, InetAddress ip){
        this.serverSocket = ss;
        this.serverAddress = ip;
    }

    @Override
    public void run() {
        try {

        while(true){

            socket = serverSocket.accept();
            }
        } catch (IOException e) {
        e.printStackTrace();
    }
    }
}
