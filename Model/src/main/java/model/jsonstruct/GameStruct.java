package model.jsonstruct;

import model.board.Board;
import model.exceptions.InvalidTurnException;
import model.exceptions.UnsupportedPieceCodeException;
import model.game.Game;
import model.game.GameState;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.player.Player;
import validation.JsonRequireRecv;
import validation.JsonRequireRecvArray;
import validation.JsonRequireRecvRecursive;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This defines the structure of a game in Json format as specified by the api
 */
public class GameStruct {
	@JsonRequireRecvArray(sizes = { 2 })
	public PlayerStruct[] players;

	@JsonRequireRecvRecursive
	public BoardStruct initialBoard;

	@JsonRequireRecv
	public Long maxTurnTime;

	public TurnStruct[] turns;

	public Integer winningPlayer;

	public Integer gameId;


	/**
	 * Turns the GameStruct attributes into a game
	 *
	 * @return a game object with the corresponding values
	 */
	public Game intoModel() throws UnsupportedPieceCodeException, InvalidTurnException {
		assert this.gameId != null;

		Board board = initialBoard.intoModel();

		AtomicReference<GamePlayerId> pid = new AtomicReference<>(GamePlayerId.PLAYER1);

		if (turns != null) {
			Arrays.stream(turns)
					.map(t -> t.intoModel(pid.getAndUpdate(GamePlayerId::other)))
					.forEachOrdered(board::applyTurn);
		}

		Game tempGame = new Game(
				Duration.ofMillis(maxTurnTime),
				new GameId(this.gameId),
				Arrays.stream(players)
						.map(PlayerStruct::intoModel)
						.toArray(Player[]::new),
				board);

		//Set winningplayer, if provided:
		if(winningPlayer != null) {
			tempGame.setWinningPlayer(GamePlayerId.fromInt(winningPlayer));
		}

		return tempGame;
	}


	/**
	 * This method turns a game-object into a Json Serializable. If the flag
	 * authenticated is true, the whole game including confidential information is
	 * stored in the JsonObject.
	 *
	 * @param authenticated determines whether all information (including
	 *                      confidential information) or a reduced set of
	 *                      information will be stored in the JsonObject
	 * @return the Serializable containing the game information
	 */
	public static GameStruct fromModel(Game g, boolean authenticated) {
		GameStruct self = new GameStruct();
		self.gameId = g.getGameId().get();
		self.players = Arrays.stream(g.getPlayers())
				.map(p -> PlayerStruct.fromModel(p, authenticated))
				.toArray(PlayerStruct[]::new);

		self.maxTurnTime = g.getMaxTurnTime().toMillis();
		self.initialBoard = BoardStruct.fromModel(g.getInitialBoard());

		if (authenticated) {
			self.turns = g.getBoard().getAppliedTurns()
					.map(TurnStruct::fromModel)
					.toArray(TurnStruct[]::new);
		}

		if (g.getGameState() == GameState.FINISHED) {
			self.winningPlayer = g.getWinningPlayer().get().get();
		}

		return self;
	}
}
