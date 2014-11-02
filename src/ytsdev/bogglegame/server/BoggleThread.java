package ytsdev.bogglegame.server;

import ytsdev.bogglegame.BoggleBoard;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The BoggleThread class. This class is the server side of a game of boggle.
 * @author Y. Stitzer
 * version 6/2/2013
 */
public class BoggleThread implements Runnable
{
    private Player[] players;
    private BoggleBoard board;

    /**
     * Constructs a new BoggleThread object with the sockets of the connected players
     * @param sockets the players' sockets
     * @throws IOException
     */
    public BoggleThread(Socket[] sockets) throws IOException
    {
        players = new Player[sockets.length];
        for(int i = 0; i < players.length; i++)
        {
            players[i] = new Player(sockets[i], sockets.length);
        }

        board = new BoggleBoard("cubes.txt");
    }

    /**
     * Directs the game, interacting with all the players
     */
    @Override
    public void run()
    {
        boolean playing = true;
        while(playing)
        {
            board.newBoard(); //reset the board

            for(int i = 0; i < players.length; i++){players[i].newRound(board.getBoardString());} //notify the players of a new game and send the board

            for(int i = 0; i < players.length; i++){players[i].retrieveWords();} //get the words from all the players

            for(int i = 0; i < players.length; i++){players[i].removeDuplicates(players, i + 1);} //remove all the words found by more than one player

            for(int i = 0; i < players.length; i++){players[i].calculatePoints();} //each player calculates their points

            for(int i = 0; i < players.length; i++){players[i].sendResults(getAllPoints());} //send all players everyone's points

            for(int i = 0; i < players.length; i++)
            {
                if(!players[i].confirmNew())//if any one player doesn't want to play again
                {
                    playing = false;
                }
            }
        }

        for(int i = 0; i < players.length; i++){players[i].gameOver();} //notify players of end of game
    }

    /**
     * Gets a string containing the names and the total points achieved by the players this round, in compliance with the protocol, which is:
     * number of players with the highest score, name and points of winner(s), names and points of everyone else
     * @return the string with the winners and points
     */
    private String getAllPoints()
    {
        String pointsStr = "";

        ArrayList<Integer> winners = new ArrayList<>(); //to hold the indexes of the winner (or winners in the event of a tie)

        winners.add(0);
        int maxInd = 0;
        for(int i = 1; i < players.length; i++)
        {
            int max = players[maxInd].getPoints();
            int current = players[i].getPoints();
            if(current > max) //found new max
            {
                winners.clear(); //remove stored indexes
                winners.add(i);
                maxInd = i;
            }
            else if(max == current) //same score
            {
                winners.add(i); //there is a tie
            }
        }

        int numWinners = winners.size(); //number of winners with highest score

        pointsStr += winners.size(); //add the number to the return string

        for(int i = 0; i < numWinners; i++) //first add the names and scores of winners
        {
            pointsStr += " " + players[winners.get(i)].getName() + " " + players[winners.get(i)].getPoints();
        }

        for(int i = 0; i < players.length; i++) //now for everyone else
        {
            if(!winners.contains(i)) //don't count the winners again
            {
                pointsStr += " " + players[i].getName() + " " + players[i].getPoints();
            }
        }

        return pointsStr; //return the final string
    }

}
