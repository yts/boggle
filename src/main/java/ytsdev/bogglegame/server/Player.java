package ytsdev.bogglegame.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * The Player class is used for the server to represent and interact with a boggle player.
 *
 * @author Y. Stitzer
 *         version 6/2/2013
 */
public class Player {
    private Scanner in;
    private PrintWriter out;

    private Set<String> words;
    private ArrayList<String> removed;
    private String name;

    private int points;

    /**
     * Constructs a Player object with the socket to interact with
     *
     * @param socket the player's socket
     * @throws java.io.IOException
     */
    public Player(Socket socket, int players) throws IOException {
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream());

        out.println(players);
        out.flush();

        name = in.nextLine();

        points = 0;
        words = new HashSet<String>();
        removed = new ArrayList<String>();
    }

    /**
     * Gets the name of the player
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the points earned by the player so far
     *
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Tells the player client to start a new round with a given board
     *
     * @param board the boggle board to use
     */
    public void newRound(String board) {
        words.clear();
        removed.clear();

        String command = "";
        command = "AGAIN " + board;
        out.println(command);
        out.flush();
    }

    /**
     * Gets the words from the client
     */
    public void retrieveWords() {
        int size = in.nextInt();
        for (int i = 0; i < size; i++) {
            words.add(in.next());
        }
    }

    /**
     * Removes a word from this Player
     *
     * @param word the word to remove if this player has it
     * @return true if this player had it and it was removed, false if the player doesn't have it
     */
    public boolean remove(String word) {
        if (words.remove(word)) {
            removed.add(word);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sends the results of the games to the player
     *
     * @param results the names and points of the other players
     */
    public void sendResults(String results) {
        String message = "";

        message += removed.size();

        for (String word : removed) {
            message += " " + word;
        }

        message += " " + results;

        out.println(message);
        out.flush();
    }

    /**
     * Calculates how many points should be added to this player for this round, and adds them
     */
    public void calculatePoints() {
        final int FIRST_REWARD = 1;
        final int SECOND_REWARD = 2;
        final int THIRD_REWARD = 3;
        final int FOURTH_REWARD = 5;
        final int FIFTH_REWARD = 11;

        for (String word : words) {
            int length = word.length();
            if (length <= 4) {
                points += FIRST_REWARD;
            } else if (length == 5) {
                points += SECOND_REWARD;
            } else if (length == 6) {
                points += THIRD_REWARD;
            } else if (length == 7) {
                points += FOURTH_REWARD;
            } else {
                points += FIFTH_REWARD;
            }
        }

    }

    /**
     * See if this player would like a new round
     *
     * @return true if this player wants to play again, false if not
     */
    public boolean confirmNew() {
        String response = in.next();
        if (response.equals("AGAIN")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tell the client no more games to be played
     */
    public void gameOver() {
        out.println("DONE");
        out.flush();

        out.close();
        in.close();
    }

    /**
     * Takes a set of words for which no players should be awarded points and removes them.
     *
     * @param duplicateWords the words submitted by at least two players
     */
    public void removeWords(Set<String> duplicateWords) {
        for (String word : duplicateWords) {
            if (words.remove(word)) {
                removed.add(word);
            }
        }
    }

    /**
     * Get a copy of the all the words guessed by the user
     *
     * @return the guessed words
     */
    public Set<String> getWords() {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(words);
        allWords.addAll(removed);

        return allWords;
    }
}
