import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * The BoggleBoard class.
 * @author Y. Stitzer
 * version 6/2/2013
 */
public class BoggleBoard
{
    private final int CUBES = 16;
    private final int SIZE = 4;

    private char[][] cubes;
    private char[][] board;

    /**
     * Creates an empty boggle board
     * @throws FileNotFoundException
     */
    public BoggleBoard() throws FileNotFoundException
    {
        board = new char[SIZE][SIZE];
        cubes = null;
    }

    /**
     * Creates a new boggle board with cubes using the the filename containing the cubes' values
     * @param cubesFile the filename of the text file with the cubes
     * @throws FileNotFoundException
     */
    public BoggleBoard(String cubesFile) throws FileNotFoundException
    {
        cubes = getCubes(cubesFile);
        board = new char[SIZE][SIZE];
    }

    /**
     * Set the board using a one-line string with the values
     * @param boardStr the board string
     */
    public void setBoard(String boardStr)
    {
        //loop through the board array
        for(int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                board[i][j] = boardStr.charAt((i * SIZE) + j);
            }
        }
    }

    /**
     * Gets a 2d array containing the cubes and their values using a given filename
     * @param filename the name of the file containing the cubes' values
     * @return the 2d array with the cubes
     * @throws FileNotFoundException
     */
    private char[][] getCubes(String filename) throws FileNotFoundException
    {

        final int SIDES = 6;
        char[][] cubes = new char[CUBES][SIDES];

        Scanner in = new Scanner(new File(filename));

        for(int i = 0; i < cubes.length; i++)
        {
            String line = in.nextLine(); //next cube
            for(int j = 0; j < cubes[i].length; j++)
            {
                cubes[i][j] = line.charAt(j); //each side of cube
            }
        }

        in.close();

        return cubes;
    }

    /**
     * Creates a new random board of letters using the cubes
     */
    public void newBoard()
    {
        char[][] board = new char[SIZE][SIZE];

        //copy the cubes to an array list to allow for removal in the next loop
        ArrayList<char[]> tempCubes = new ArrayList<>();
        for(char[] cube : this.cubes)
        {
            tempCubes.add(cube);
        }

        Random rand = new Random();
        //loop through each space on the board
        for(int i = 0; i < board.length; i++)
        {
            for(int j = 0; j < board[i].length; j++)
            {
                char[] cube = tempCubes.remove(rand.nextInt(tempCubes.size())); //select a random cube to use and then remove it
                board[i][j] = cube[rand.nextInt(cube.length)]; //select a random side of the cube to use
            }
        }

        this.board = board;
    }

    /**
     * Rotates the board clockwise for user's convenience
     */
    public void rotateRight()
    {
        char[][] newBoard = new char[SIZE][SIZE];

        //goes down the rows, assigning the values to the columns from right to left
        for(int i = 0; i < newBoard.length; i++)
        {
            for(int j = 0; j < newBoard[i].length; j++)
            {
                newBoard[j][(SIZE-1) - i] = board[i][j];
            }
        }

        board = newBoard;
    }

    /**
     * Rotates the board counter-clockwise for user's convenience
     */
    public void rotateLeft()
    {
        char[][] newBoard = new char[SIZE][SIZE];

        //moves down the rows, assigning the values to the columns from left to right
        for(int i = 0; i < newBoard.length; i++)
        {
            for(int j = 0; j < newBoard[i].length; j++)
            {
                newBoard[(SIZE-1) - j][i] = board[i][j];
            }
        }

        board = newBoard;
    }

    /**
     * Gets a string with the current board's values formatted in a straight line
     * @return
     */
    public String getBoardString()
    {
        String str = "";
        for(char[] row : board)
        {
            for(char letter : row)
            {
                str += letter;
            }
        }

        return str;
    }

    /**
     * Determines whether a given word can be spelled using the current letters on the board
     * @param word the word to find
     * @return true if the word can be spelled, false if not
     */
    public boolean contains(String word)
    {
        word = word.toUpperCase();

        //If word contains a QU, remove the U to test against the board (the board array uses a 'Q' for Qu)
        int QIndex = word.indexOf('Q');
        if(QIndex != -1)
        {
            if(QIndex + 2 < word.length())
            {
                word = word.substring(0, QIndex + 1) + word.substring(QIndex + 2); //remove the u
            }
            else
            {
                //impossible to not have two letters after a Q in boggle
                return false;
            }
        }

        //starting from every cube, try to find the word
        for(int row = 0; row < board.length; row++)
        {
            for(int col = 0; col < board[row].length; col++)
            {
                if(canSpell(row, col, word))
                {
                    return true;
                }
            }
        }
        //never found it
        return false;
    }

    /**
     * Determines whether a word can be spelled on the current boggle board, starting from a specified location
     * @param row the row position to start from
     * @param col the column position to start from
     * @param word the word to find
     * @return true if the word can be spelled, false if not
     */
    private boolean canSpell(int row, int col, String word)
    {
        if(board[row][col] != word.charAt(0)) //wrong letter or marked with '?'
        {
            return false; //count go this way
        }

        if(word.length() <= 1) //this is the correct letter and this is the last
        {
            return true;
        }

        //temporarily remove character to avoid using the same letter twice
        char value = board[row][col];
        board[row][col] = '?';

        //moves to all the surrounding cubes on the board to find next characters in the word
        for(int i = row - 1; i <= row + 1; i++)
        {
            for(int j = col - 1; j <= col + 1; j++)
            {
                if(i >= 0 && j >= 0 && //prevent out of bounds
                        i < board.length && j < board[row].length && //prevent out of bounds
                        !(i == row && j == col) && //don't test itself
                        canSpell(i, j, word.substring(1)))
                {
                    board[row][col] = value; //put the letter back on the board
                    return true;
                }
            }
        }

        board[row][col] = value;
        return false; //canSpell never returned true
    }

    /**
     * Gets a string displaying the board
     */
    public String toString()
    {
        String str = "";
        for(char[] row : board)
        {
            String rowStr = Arrays.toString(row);

            //display U's with the Q's on the board
            int QIndex = rowStr.indexOf('Q');
            if(QIndex != -1)
            {
                rowStr = rowStr.substring(0, QIndex + 1) + "U" + rowStr.substring(QIndex + 1);;
            }

            str += rowStr + "\n";
        }

        return str;
    }

}
