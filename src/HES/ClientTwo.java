package HES;

import java.io.IOException;

public class ClientTwo extends ClientAlpha {


    public ClientTwo() throws IOException {

        clientName="Client two";

        //portClient=22545;
        try {
            startClientSockets();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    // ClientTwo a le code du Server et du Client
    //ClientTwo lit l'info du Server et se connect aux path et le fichier audio
    // après le ClientTwo envoi le path et le nom de son fichier audion à Server
}
