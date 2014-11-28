package ytsdev.bogglegame.client;

import ytsdev.bogglegame.BoggleBoard;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Game class. This class interacts with the server and user to play the game of boggle.
 *
 * @author Y. Stitzer
 *         version 6/2/2013
 */
public class Game {
    private PrintWriter out;
    private Scanner in;
    private Scanner userIn;

    private int players;
    private Lock lock;
    private boolean isTime; //true if game is still in play
    private String name;

    private BoggleBoard board;
    private String[] dictionary;
    private Set<String> words;

    public Game(Socket server) throws IOException {
        out = new PrintWriter(server.getOutputStream());
        in = new Scanner(server.getInputStream());
        userIn = new Scanner(System.in);

        board = new BoggleBoard();
        dictionary = getDictionary();
        words = new HashSet<String>();
        isTime = true;
        lock = new ReentrantLock();

        System.out.println("Please enter your first name:");
        StringTokenizer nameTok = new StringTokenizer(userIn.nextLine());
        name = nameTok.nextToken();
        System.out.println("Thank you, " + name + ". Waiting for players to connect...");

        players = in.nextInt();
        out.println(name);
        out.flush();

        in.next(); //get rid of extra "AGAIN"
    }

    /**
     * Gets the board from the server and starts a new round of boggle
     */
    public void newGame() {
        board.setBoard(in.next()); //get board string from server

        System.out.println("Your new game will start momentarily. Get ready! You have three minutes to get your words in!");
        //pause to give users time
        final int PAUSE = 2000;
        try {
            Thread.sleep(PAUSE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println(board);

        startTimer();

        while (isTime) {
            String input = userIn.next();
            processInput(input);
        }

    }

    /**
     * Starts the boggle timer which will stop the game when the time runs out.
     */
    private void startTimer() {
        class Timer implements Runnable {
            @Override
            /**
             * Stops the game when the time runs out.
             */
            public void run() {
                final int TIME = 180000;

                try {
                    Thread.sleep(TIME);
                    stopGame();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Thread timer = new Thread(new Timer());
        timer.start();
    }

    /**
     * Processes input from the user during the game (rotating, adding words) and gets user's response to being prompt for a new game.
     *
     * @param input the user's input
     */
    private void processInput(String input) {
        lock.lock();
        if (isTime) {
            if (input.equals("1")) {
                board.rotateLeft();
                System.out.println(board);
            } else if (input.equals("2")) {
                board.rotateRight();
                System.out.println(board);
            } else {
                if (inDictionary(input)) //confirm that word is valid
                {
                    if (board.contains(input)) //confirm that it's found on this board
                    {
                        words.add(input);
                    } else {
                        System.out.println(input + " cannot be spelled on this board.");
                    }
                } else {
                    System.out.println(input + " is not a valid boggle word.");
                }
            }

            lock.unlock();
        } else //time is up and user has already been prompt for new game
        {
            lock.unlock();
            requestNewGame(input);
        }
    }

    /**
     * Ends the round and performs the post-game actions. User is then prompt for a new game. The user's response is not processed here.
     *
     * @throws java.io.IOException
     */
    public void stopGame() throws IOException {
        lock.lock();
        isTime = false;
        System.out.println("Time up!");
        submitWords();
        getResults();
        System.out.println("Would you like to start a new round?(Y/N)");
        lock.unlock();
    }

    /**
     * Sends the list of words found to the server. The format is: number of words, and then the words
     */
    private void submitWords() {
        String message = "";

        message += words.size();

        for (String word : words) {
            message += " " + word;
        }

        out.println(message);
        out.flush();
    }

    /**
     * Gets the results of the last round and displays it. This includes the words found by other users and the accumulated scores for all the users.
     */
    private void getResults() {
        int same = in.nextInt(); //number of words found by other users
        if (same != 0) {
            System.out.println("Other players also had the following " + same + " word(s), so no one is rewarded points for them:");
            for (int i = 0; i < same; i++) {
                System.out.println(in.next());
            }
        }

        System.out.println("The highest points are rewarded to:");
        int winners = in.nextInt(); //number of players with the highest achieved points
        for (int i = 0; i < winners; i++) //display the winners' names and scores
        {
            String name = in.next();
            if (name.equals(this.name)) {
                name = "You";
            }
            System.out.println(name + " with " + in.next() + " points!");
        }

        if (players - winners != 0) //if there are players with lower scores
        {
            System.out.println("Here are the other scores:");
            for (int i = 0; i < (players - winners); i++) //display the other scores
            {
                String name = in.next();
                if (name.equals(this.name)) {
                    name = "You";
                }
                System.out.println(name + " with " + in.next() + " points.");
            }
        }
    }

    /**
     * Determines based on the user's and server's response whether to start a new round or not.
     * Sends the message "AGAIN" to the server if a new game is requested. Expects "AGAIN" from server for a new game, or "DONE" if no more games are to be played.
     *
     * @param input the user's response after being prompted for a new game.
     */
    private void requestNewGame(String input) {
        if (input.equalsIgnoreCase("Y")) {
            out.println("AGAIN");
            out.flush();

            String response = in.next();
            if (response.equals("AGAIN")) //all other users agreed
            {
                words.clear();
                isTime = true;
                newGame();
            } else {
                System.out.println("Another user ended the game. Thank you for playing!");
            }
        } else if (input.equalsIgnoreCase("N")) {
            out.println("DONE");
            out.flush();
            System.out.println("Thank you for playing!");
        } else //invalid input
        {
            System.out.println("Invalid response.");
            requestNewGame(userIn.next());
        }
    }

    /**
     * Gets an array with all the words in the boggle dictionary text file.
     *
     * @return the String array with the dictionary words.
     */
    private String[] getDictionary() {
        final int WORDS = 81090;
        String[] dict = new String[WORDS];
        try {
            BufferedReader file = new BufferedReader(new FileReader(new File("BoggleWords.txt")));
            for (int i = 0; i < WORDS; i++) {
                dict[i] = file.readLine();
            }

            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dict;
    }

    /**
     * Determines if a word is in the dictionary of not.
     *
     * @param word the word to look for
     * @return true if it is found, false if not.
     */
    private boolean inDictionary(String word) {
        word = word.toLowerCase();

        return Arrays.binarySearch(dictionary, word) >= 0;
    }


}
