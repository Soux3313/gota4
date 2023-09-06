package model.game;

import model.board.Board;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.player.Player;
import model.player.Turn;

import java.time.Duration;
import java.util.Optional;

/**
 * This objects describes the status of the current game. This includes
 * information on the {@link Player} , timing rules and the {@link Board}.
 *
 *
 */
public class Game {

	/**
	 * The game ID
	 */
	private GameId gameId;

	/**
	 * The list of {@link Player}s
	 */
	private final Player[] players;

	private final Board initialBoard;

	/**
	 * A representation of the {@link Board}
	 */
	private final Board board;

	/**
	 * Maximum amount of time the server is waiting for a response for a {@link Turn}
	 * request (in ms)
	 */
	private final Duration maxTurnTime;

	/**
	 * ID of the {@link Player} that won the Game
	 */
	private Optional<GamePlayerId> winningPlayerId = Optional.empty();


	/**
	 * Internal game status used in the game logic
	 */
	private GameState currentGameStatus;

	/**
	 * Constructor for a Game
	 * @param maxTurnTime Maximum amount of time the server is waiting for a response for a {@link Turn} request (in ms).
	 * @param gameId The Id of the Game
	 * @param players the {@link Player}s involved in this Game
	 * @param board the {@link Board} of this Game.
	 */
	public Game(Duration maxTurnTime, GameId gameId, Player[] players, Board board) {
		assert players.length == 2;

		this.maxTurnTime = maxTurnTime;
		this.gameId = gameId;
		this.players = players;
		this.board = board.clone();
		this.initialBoard = board;
		this.currentGameStatus = GameState.STARTING;
	}

	/**
	 * fetches the {@link Player} corresponding to its id in this game
	 *
	 * @param pid the player id of the player to fetch
	 * @return the associated {@link Player}
	 */
	public Player getPlayer(GamePlayerId pid) {
		return this.players[pid.get()];
	}

	/**
	 * @return the player who is currently making a turn
	 */
	public Optional<GamePlayerId> getCurrentPlayerId() {
		if (this.getGameState() == GameState.TURN_PLAYER1) {
			return Optional.of(GamePlayerId.PLAYER1);
		} else if (this.getGameState() == GameState.TURN_PLAYER2) {
			return Optional.of(GamePlayerId.PLAYER2);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * @return an array of the 2 players playing in this game
	 */
	public Player[] getPlayers() {
		return players;
	}

	public Board getInitialBoard() {
		return this.initialBoard;
	}

	public Board getBoard() {
		return board;
	}

	public Duration getMaxTurnTime() {
		return maxTurnTime;
	}

	public GameId getGameId() {
		return gameId;
	}

	public void setGameId(GameId gameId) {
		this.gameId = gameId;
	}

	/**
	 * @return the player who won the game, if the game is finished
	 */
	public Optional<GamePlayerId> getWinningPlayer() {
		return this.winningPlayerId;
	}

	/**
	 * Sets the winningPlayerId of the Game
	 * 
	 * @param id the Id of the winning {@link Player}. The Id must 0 or 1.
	 */
	public void setWinningPlayer(GamePlayerId id) {
		this.winningPlayerId = Optional.of(id);
		this.currentGameStatus = GameState.FINISHED;
	}

	/**
	 * Return the current status of the Game
	 * @return the current status of the Game
	 */
	public GameState getGameState() {
		return this.currentGameStatus;
	}

	/**
	 * Switches GameState to GameState.TURN_PLAYERONE
	 */
	public void turnPlayerOne() {
		this.currentGameStatus = GameState.TURN_PLAYER1;
	}

	/**
	 * Switches GameState to GameState.TURN_PLAYERTWO
	 */
	public void turnPlayerTwo() {
		this.currentGameStatus = GameState.TURN_PLAYER2;
	}

	/**
	 * Switches GameState to GameState.ABORTED
	 */
	public void abort() {
		this.currentGameStatus = GameState.ABORTED;
	}

	/**
	 * Switches GameState to GameState.FINISHED
	 */
	public void finish(GamePlayerId pid) {
		this.currentGameStatus = GameState.FINISHED;
		setWinningPlayer(pid);
	}
}
