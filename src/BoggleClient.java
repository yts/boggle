import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * The client program
 * @author Y. Stitzer
 * version 6/2/2013
 */
public class BoggleClient
{
static Date date;
static long startTime;

	/**
	 * @param args
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException
	{
				final String HOSTNAME = "localhost";
			   final int PORT = 2983;
				Socket server = new Socket(HOSTNAME, PORT);

				System.out.println("_-_-_-_-WELCOME TO BOGGLE!-_-_-_-_");
				System.out.println("When the board is displayed, type in your words and hit ENTER.\nTo rotate the board to the left, type \"1\", and to the right with \"2.\"");
				Game game = new Game(server);
				game.newGame();
				server.close();
	}

}
