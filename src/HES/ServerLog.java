package HES;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerLog {

    /**
     * @author Thomas/Marina
     * Classe servant à regrouper toutes les interactions avec le log
     */

    private String myLog = "c://temp//AudioStream//my.log";


    public ServerLog(){


        printList();

    }


    public void printList (){


        Logger log = Logger.getLogger("myLogger");
        try {
            FileHandler f = new FileHandler(myLog, true);
            // SimpleFormatter formatter = new SimpleFormatter();
            CustomFormatter formatter = new CustomFormatter();

            log.addHandler(f);
            f.setFormatter(formatter);
            log.setLevel(Level.INFO);
            log.info("\n-------------this is the info level------");
            log.warning("attention hacker");
            log.severe("exception");

            log.setLevel(Level.WARNING);
            log.warning("\n-------------this is the warning level------");
            log.warning("attention hacker");
            log.severe("exception");

            log.setLevel(Level.SEVERE);
            log.severe("\n-------------this is severe level------");
            log.warning("attention hacker");
            log.severe("exception");

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

}
