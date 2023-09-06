package model.game;

import model.board.Board;
import model.exceptions.InvalidTurnException;
import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.ids.GlobalPlayerId;
import model.player.Move;
import model.player.Player;
import model.player.Shot;
import model.player.Turn;
import model.util.PieceMap;
import model.util.Position;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Duration;
import java.util.Optional;

/** This class tests most of the methods of Game
 */

public class GameTest {
	
	// The format to address this is int[row][column], with [0][0] being at the top left corner.
	// Defining this here to avoid retyping this several times, a generic int-array with the piececodes -1, 0 and 1 (-2 missing)
	final Integer[][] testSquares = new Integer[][] {
		{ 0,   1, -1,  1, -1, -1,  1, -1, -1, -1},
		{ -1, -1, -1, -1, -2, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  1, -1, -1, -1, -1, 0, -1, -1, -1,  1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{  0, -1, -1, -1, -1, -1, -1, -1, -1,  -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		{ -1, -1, -1,  -1, -1, -1,  0, -1, -1, -1}};
		
	Board board = new Board(PieceMap.fromSquares(10, 10, testSquares));

	public GameTest() throws UnsupportedPieceCodeException {
	}

	@Test
	public void GameConstructTest() {
		//tests if the constructor is working as intended
		Player p1 = new Player(new GlobalPlayerId(0),"Peter","http://www.omfgdogs.com");
		Player p2 = new Player(new GlobalPlayerId(1), "Hans", "http://www.youtube.com");
		Player[] playerArray = new Player[] {p1, p2};
		
		Game g1 = new Game(Duration.ofMillis(200), new GameId(24), playerArray, board);
		Game g2 = new Game(Duration.ofMillis(10), new GameId(1), playerArray, board);
	}
	
	@Test
	public void getLastTurn() throws InvalidTurnException {
		Player p1 = new Player(new GlobalPlayerId(0),"Peter","http://www.omfgdogs.com");
		Player p2 = new Player(new GlobalPlayerId(1), "Hans", "http://www.youtube.com");
		Player[] playerArray = new Player[] {p1, p2};
		Game g1 = new Game(Duration.ofMillis(200), new GameId(24), playerArray, board);
		assertEquals(g1.getBoard().getLastTurn(), Optional.empty());

		Turn t1 = new Turn(new Move(new Position(0,0),new Position(1,1)),new Shot(new Position(0,0)), GamePlayerId.PLAYER1);
		g1.getBoard().applyTurn(t1);
		assertEquals(t1,g1.getBoard().getLastTurn().get());
		Turn t2 = new Turn(new Move(new Position(1,1),new Position(1,2)),new Shot(new Position(1,1)), GamePlayerId.PLAYER1);
		g1.getBoard().applyTurn(t2);
		assertEquals(t2,g1.getBoard().getLastTurn().get());
	}

	@Test
	public void applyTurnTest() throws InvalidTurnException {
		Player p1 = new Player(new GlobalPlayerId(0),"Peter","http://www.omfgdogs.com");
		Player p2 = new Player(new GlobalPlayerId(1), "Hans", "http://www.youtube.com");
		Player[] playerArray = new Player[] {p1, p2};
		Game g1 = new Game(Duration.ofMillis(200), new GameId(24), playerArray, board);
		Turn t1 = new Turn(new Move(new Position(0,0),new Position(1,1)),new Shot(new Position(0,0)), GamePlayerId.PLAYER1);


		/* maybe should check for Position instead of Turn (we don't want to move again)
		Turn t2 = new Turn(new Move(new Position(1,1),new Position(2,2)),new Shot(new Position(3,3)), GamePlayerId.PLAYER1);
		g1.applyTurn(t1);
		assertEquals(t2, g1.getLastTurn().get());
		 */
	}

	/* WIP
	@Test
	public void getNextTurnIdTest() {
		final ArrayList<Turn> turns = new ArrayList<Turn>();
		new TurnId(turns.size());
		assertEquals(turns.size() + 1, turns.contains(turns.size()));
	}
	 */
}