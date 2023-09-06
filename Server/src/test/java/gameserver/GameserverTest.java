package gameserver;

import model.board.Board;
import model.exceptions.GameIdAlreadyInUseException;
import model.exceptions.PlayerIdAlreadyInUseException;
import model.exceptions.UnsupportedPieceCodeException;
import model.game.Game;
import model.ids.GameId;
import model.ids.GlobalPlayerId;
import model.player.Player;
import model.util.PieceMap;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests all methods of the gameserver class
 *
 */
public class GameserverTest {
	
	/**
	 * Tests
	 * @throws PlayerIdAlreadyInUseException, GameIdAlreadyInUseException 
	 *
	 */
	@Test
	public void test() throws PlayerIdAlreadyInUseException, GameIdAlreadyInUseException, UnsupportedPieceCodeException {
		// creating a functional server (should work)

		GameserverOpts opts = new GameserverOpts("localhost", 8000,"hackeraccesstoken", false,true, true);
		Gameserver testserver1 = new Gameserver(opts);
		
		// creating a server with faulty values. Program should terminate
		// Gameserver testserver2 = new Gameserver("p", 10);
		// Gameserver testserver3 = new Gameserver("localhost", -10);
			
		// Create two Players to add them, testplayers array will be used for game object
		Player player1 = new Player(new GlobalPlayerId(2), "Hans", "omfgdogs.com");
		Player player2 = new Player(new GlobalPlayerId(3), "Peter", "omfgdogs.com");
		Player[] testplayers = {player1, player2};

		testserver1.addPlayer(player1);
		testserver1.addPlayer(player2);
		
		// testing doesPlayerExistById() explicitly
		assertTrue(testserver1.doesPlayerExistById(new GlobalPlayerId(2)));
		
		// making sure removePlayer() removes a Player
		testserver1.removePlayer(new GlobalPlayerId(2));
		assertFalse(testserver1.doesPlayerExistById(new GlobalPlayerId(2)));
		
		// should throw an PlayerIdAlreadyInUseException as we already added player2
		try {
			testserver1.addPlayer(player2);
		} catch (PlayerIdAlreadyInUseException e) {
			// Auto-generated catch block
			System.out.println(e.toString());
			System.out.println("addPlayer throws correct Exception");
		}
		
		// Creating a Board in order to be able to create Games to test addGame...
		Integer[][] testArray = { {-1,-1}, {-1,-1}};
		Board testboard = new Board(PieceMap.fromSquares(2, 2, testArray));
		Game testgame1 = new Game(Duration.ofSeconds(5), new GameId(1), testplayers, testboard);
		Game testgame2 = new Game(Duration.ofSeconds(5), new GameId(2), testplayers, testboard);
		
		testserver1.addGame(testgame1);
		testserver1.addGame(testgame2);
		
		// This should throw an exception as well as we already have that game
		try {
			testserver1.addGame(testgame1);
		} catch (GameIdAlreadyInUseException e) {
			// Auto-generated catch block
			System.out.println(e.toString());
			System.out.println("addGame throws correct Exception");
		}
		
		// testing doesGameExistByID explicitly
		assertTrue(testserver1.doesGameExistById(new GameId(1)));
		
		// Making sure that removing the game actually removes it
		testserver1.removeGame(new GameId(1));
		assertFalse(testserver1.doesGameExistById(new GameId(1)));

		// JSON-Methods, for now we will just run it with testgame2 in its gamelist
		/*System.out.println("allGamesToJson: false");
		testserver1.allGamesToJson(false);
		System.out.println("allGamesToJson: true");
		testserver1.allGamesToJson(true);
		
		// same again but with an empty Gamelist
		testserver1.removeGame(2);
		System.out.println("allGamesToJson: false");
		testserver1.allGamesToJson(false);
		System.out.println("allGamesToJson: true");
		testserver1.allGamesToJson(true);*/

	}

}
