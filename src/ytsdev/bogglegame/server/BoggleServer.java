package ytsdev.bogglegame.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The BoggleServer class. Listens for connections and starts games when enough players join
 *
 * @author Y. Stitzer
 * @version 6/2/2013
 */
public class BoggleServer {
    public static void main(String[] args) throws IOException {
        final int PORT = 2983;
        ServerSocket server = new ServerSocket(PORT);

        boolean listening = true;
        while (listening) {
            final int PLAYERS = 2; //change this to adjust the number of players in the game
            Socket[] players = new Socket[PLAYERS];
            for (int i = 0; i < PLAYERS; i++) {
                players[i] = server.accept();
            }
            BoggleThread game = new BoggleThread(players);
            Thread thread = new Thread(game);
            thread.start();
        }

        server.close();
    }

}
