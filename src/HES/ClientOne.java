package HES;

public class ClientOne extends ClientAlpha {

    /**
     * @author Thomas
     * Classe servant a simuler l'utilisation de deux clients sur deux pc différents
     * En l'absence d'une gestion total avec des threads nous avons utilise un fonctionnement sequentiel
     * Cette classe represente un client choisissant un audio aupres d'un autre client pour l'ecouter
     */

    public ClientOne()  {

        myMusicRepertory="C://temp//AudioStream//myMusic";
        portClientClient = 25245;

        start();
        sendSomethingToSomeone(exchangeSocket, musicChoice());
        askClientForAnAudio();

    }
}
