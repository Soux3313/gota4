package gameserver;

import com.sun.net.httpserver.HttpServer;
import gameserver.controller.CommunicationHandler;
import gameserver.controller.GamesHandler;
import gameserver.controller.PlayerHandler;
import gameserver.logger.Logger;
import gameserver.result.GameRemoveResult;
import gameserver.result.PlayerRemoveResult;
import gameserver.sync.LockGuard;
import gameserver.sync.OwningLock;
import https.HttpServerFactory;
import model.board.Board;
import model.exceptions.GameIdAlreadyInUseException;
import model.exceptions.PlayerIdAlreadyInUseException;
import model.game.Game;
import model.ids.GameId;
import model.ids.GlobalPlayerId;
import model.player.Player;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * This class represents the REST-API and the HttpServer.
 *
 */
public class Gameserver {

	/**
	 * This is the HttpServer object the 'Spielserver' relies on
	 */
	private final HttpServer server;


	/**
	 * the logger instance for this gameserver
	 */
	private final Logger logger;


	/**
	 * the configuration of the server
	 */
	private final GameserverOpts opts;

	private final HashMap<String, CommunicationHandler> routeListeners = new HashMap<>();

	// Im not sure, if this is really needed here or if the Game-Object should
	// store these
	private final TreeMap<GlobalPlayerId, Player> players = new TreeMap<>();

	/**
	 * Stores all games on the server
	 */
	private final Map<GameId, OwningLock<Game>> games = new ConcurrentHashMap<>();

	/**
	 * Stores all gameThreads on the server
	 */
	private final Map<GameId, GameThread> gameThreads = new ConcurrentHashMap<>();

	/**
	 * Creates a new 'Spielserver'-Object that runs the 'Amazonen Spiel'-API
	 * 
	 * @param opts the configuration of the server
	 */
	public Gameserver(GameserverOpts opts) {
		this.opts = opts;
		this.logger = new Logger(opts.debugMode, opts.logTraffic);

		if (opts.insecure) {
			this.server = HttpServerFactory.makeHttpServerOrDie(new InetSocketAddress(opts.hostname, opts.port));
		} else {
			this.server = HttpServerFactory.makeHttpsServerOrDie(new InetSocketAddress(opts.hostname, opts.port));
		}

		this.server.createContext("/players", new PlayerHandler(this));
		this.server.createContext("/games", new GamesHandler(this));

		logger.debug("Server created");
	}

	/**
	 * This method starts the server
	 */
	public void start() {
		logger.debug("Starting Gameserver");
		this.server.start();
		logger.info("Listening on %s://%s:%d", opts.insecure ? "http" : "https", opts.hostname, opts.port);
	}

	public void stop() {
		logger.debug("Stopping Gameserver");
		this.server.stop(0);
	}


	/**
	 * Adds a player to the list of registered players on this server.
	 * 
	 * @param p the player object to be added
	 * @throws PlayerIdAlreadyInUseException in case the given id is already in use.
	 */
	public void addPlayer(Player p) throws PlayerIdAlreadyInUseException {
		// check if id is already in use:
		if (!doesPlayerExistById(p.getPlayerId())) {
			// Id is not in use
			players.put(p.getPlayerId(), p);
		} else {
			throw new PlayerIdAlreadyInUseException("The id of the Player is already in use.", p);
		}
	}

	/**
	 * Removes all registered players with the given id
	 * this must be the most inefficient piece of code in this whole project
	 *
	 * @param playerId The id of those players to be deleted.
	 * @return PlayerRemoveResult based on the current state
	 */
	public PlayerRemoveResult removePlayer(GlobalPlayerId playerId) {
		boolean playerInGame = games.values().stream()
				.anyMatch(game -> {
					try (LockGuard<Game> g = game.lock()) {
						return Arrays.stream(g.get().getPlayers())
								.anyMatch(player -> player.getPlayerId().equals(playerId));
					}
				});

		if (playerInGame) {
			return PlayerRemoveResult.CANNOT_REMOVE_IN_GAME;
		} else {
			players.remove(playerId);
			return PlayerRemoveResult.REMOVED;
		}
	}

	/**
	 * This methods checks, if there is a player registered that has the given
	 * playerId
	 * 
	 * @param playerId the playerId to check
	 * @return true, if there is player in the player-list that has the given Id,
	 *         false if not.
	 */
	public boolean doesPlayerExistById(GlobalPlayerId playerId) {
		// check if id is already in use:
		return players.containsKey(playerId);
	}

	/**
	 * Adds a game to the list of registered players in this server.
	 * 
	 * @param g the game object to be added
	 * @throws GameIdAlreadyInUseException in case the given id is already in use.
	 */
	public void addGame(Game g) throws GameIdAlreadyInUseException {
		// check if id already in use:
		if (!doesGameExistById(g.getGameId())) {
			games.put(g.getGameId(), new OwningLock<>(new ReentrantLock(), g));
		} else {
			throw new GameIdAlreadyInUseException("The id of the Game is already in use.", g.getGameId());
		}

		GameThread gameThread = new GameThread(g.getGameId(), this);
		this.gameThreads.put(g.getGameId(), gameThread);
		gameThread.start();
	}

	/**
	 * Removes all registered games with the given id.
	 * 
	 * @param gameId The id of those games to be deleted.
	 */
	public GameRemoveResult removeGame(GameId gameId) {
		GameThread thrd = gameThreads.remove(gameId);

		boolean interrupted = false;
		if (thrd.isAlive()) {
			thrd.interrupt();
			interrupted = true;
		}

		try {
			thrd.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		games.remove(gameId);

		return interrupted ? GameRemoveResult.INTERRUPTED_AND_REMOVED : GameRemoveResult.REMOVED;
	}

	public boolean doesGameExistById(GameId gameId) {
		return games.containsKey(gameId);
	}


	@Deprecated
	public void createGame(GameId gameId, Duration maxTurnTime, Player playerOne, Player playerTwo, Board intialBoard) throws GameIdAlreadyInUseException {
		Player[] players = new Player[2];
		players[0] = playerOne;
		players[1] = playerTwo;

		Game newGame = new Game(maxTurnTime, gameId, players, intialBoard);
		this.addGame(newGame);
	}

	public Stream<Player> getPlayers() {
		return players.values().stream();
	}

	public Stream<OwningLock<Game>> getGames() {
		return games.values().stream();
	}

	public Optional<OwningLock<Game>> getGameByID(GameId gameId) {
		return Optional.ofNullable(games.get(gameId));
	}

	public GameserverOpts getOpts() {
		return opts;
	}

	public Logger getLogger() {
		return this.logger;
	}
}
