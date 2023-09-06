package tournamentmanager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import https.HttpClientFactory;
import model.board.Board;
import model.game.Game;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.ids.GlobalPlayerId;
import model.jsonstruct.BoardStruct;
import model.jsonstruct.GameStruct;
import model.jsonstruct.GetGamesResponse;
import model.jsonstruct.ReducedGameStruct;
import model.player.Player;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import tournamentmanager.backup.BackupManager;
import tournamentmanager.exceptions.PlayerNotAvailableException;
import tournamentmanager.exceptions.ServerNotAvailableException;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class of the TournamentManager
 *
 */
public class TournamentManager {
	
	// max turn time in ms
	private final Duration maxTurnTime;
	
	// starting board for all the games getting created
	private final Board startingBoard;

	// server address to connect to
	private final String serverAddress;

	// token to be used in requests
	private final String serverToken;
	
	// BackupManager instance used for the backup process
	private final BackupManager backupManager;
	
	// all players
	private ArrayList<Player> allPlayers;
	
	// available players currently not in a game
	private final ArrayList<Player> playerPool;
	
	// stores game availability
	// true means game has not been started or finished, false means game has already been started or finished
	private boolean[][] gamesAvailable;
	
	// counter used to create new gameIds
	private int nextGameId;
	
	// the gamewatchingthreads running atm
	private final ArrayList<GameWatchingThread> runningWatchingThreads;
	
	private final RequestConfig requestConfig;
	
	// used to make sure poolUpdate() is only running once at a time
	private boolean poolUpdatingRunning = false;
	
	// will prevent poolUpdate from running when we are currently restoring the server
	private boolean serverIsDead = false;
	
	private final ConcurrentLinkedQueue<GameWatchingThread> gameFinishedQueue = new ConcurrentLinkedQueue<>();
	private boolean gameFinishedQueueRunning = false;

	public TournamentManager(Duration maxTurnTime, Board startingBoard, String serverAddress, String serverToken, String backupDirectoryPath) {
		this.maxTurnTime = maxTurnTime;
		this.startingBoard = startingBoard;
		
		this.serverAddress = serverAddress;
		this.serverToken = serverToken; 
		
		this.backupManager = new BackupManager(backupDirectoryPath);
		
		this.playerPool = new ArrayList<>();
		this.runningWatchingThreads = new ArrayList<>();
		
		this.nextGameId = 1;
		
		// we allow a timeout of 5 seconds
		this.requestConfig = RequestConfig.custom()
				.setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000)
				.setSocketTimeout(5000).build();
	}
	
	public RequestConfig getRequestConfig() {
		return this.requestConfig;
	}


	public static Stream<Player> parsePlayersFile(InputStream is) throws IOException {
		String playersFile = new String(is.readAllBytes());
		AtomicInteger id = new AtomicInteger(0);

		return Arrays.stream(playersFile.split("\\r?\\n"))
				.map(line -> {
					String[] data = line.split(",");
					return new Player(new GlobalPlayerId(id.getAndIncrement()), data[0], data[1]);
				});
	}

	/**
	 * Parses the players from the input file and then starts a tournament on the server
	 * @param players
	 */
	public void startTournament(Stream<Player> players) {
		//Creates players from file content
		this.allPlayers = players.collect(Collectors.toCollection(ArrayList<Player>::new));
		
		// server needs to be "clean"
		try {
			this.prepareServer();
		} catch (ServerNotAvailableException e) {
			System.out.println("[startWithPlayersFile] Server not available for preparation");
			
			this.serverHasDiedEvent();
			return;
		}	
		
		// all players need to be reachable
		this.checkAvailability(this.allPlayers);
	
		// checks if there are games that need to be restored
		try {
			this.checkForRestoreAndStart();
		} catch (ServerNotAvailableException e) {
			System.out.println("[startWithPlayersFile] Server not available for restoration");
			this.serverHasDiedEvent();
		}		
	}
	
	/**
	 * Resets the gameserver by deleting all existing games
	 */
	private void prepareServer() {
		// load all games
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		String getUrl = String.format("%s/games", this.serverAddress);
		
		HttpGet get = new HttpGet(getUrl);
		get.setConfig(this.requestConfig);
		
		System.out.println("[prepareServer] Preparing..");
		
		JsonValidatingParser parser = new JsonValidatingParser();		
		ReducedGameStruct[] parsedStructs;
		try {
			
			HttpResponse response = client.execute(get);
			String body = new String(response.getEntity().getContent().readAllBytes());
			
			GetGamesResponse parsedResponse = parser.fromJson(body, GetGamesResponse.class);
			parsedStructs = parsedResponse.games;
		} catch (JsonParseException | JsonValidationException e) {
			System.out.println("[prepareServer] The validation of the server response failed");
			e.printStackTrace();
			
			throw new ServerNotAvailableException();
		} catch (SocketTimeoutException e) {
			System.out.println("[prepareServer] Got a timeout");
			
			throw new ServerNotAvailableException();
		} catch(HttpHostConnectException e) {
			System.out.println("[prepareServer] Failed to open connection");
			
			throw new ServerNotAvailableException();
		} catch (IOException c) {
			System.out.println("[prepareServer] Got an IOException");
			c.printStackTrace();
			
			throw new ServerNotAvailableException();
		} finally {
			try {
				client.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
		
		// delete the games
		
		for(ReducedGameStruct struct : parsedStructs) {
			this.deleteGameOnServer(new GameId(struct.gameId));
		}
		
		System.out.println("[prepareServer] Server is ready!");
	}
	
	/**
	 * Method used to make a delete request to the server
	 * @param gameId
	 */
	private void deleteGameOnServer(GameId gameId) throws ServerNotAvailableException {	
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		String deleteUrl = String.format("%s/games/%d?token=%s",
				this.serverAddress,
				gameId.get(),
				this.serverToken);

		HttpDelete delete = new HttpDelete(deleteUrl);
		delete.setConfig(this.requestConfig);
		
		try {
			HttpResponse resp = client.execute(delete);
			 
			if(resp.getStatusLine().getStatusCode() != 200) {
				System.out.println("[deleteGameOnServer] Invalid token provided, or server implements api incorrectly");
				System.exit(1);
				return;
			}
		} catch (SocketTimeoutException e) {
			System.out.println("[deleteGameOnServer] Got a timeout");
			
			throw new ServerNotAvailableException();
		} catch (IOException e) {
			System.out.println("[deleteGameOnServer] Got an IOException");
			e.printStackTrace();
			
			throw new ServerNotAvailableException();
		} catch (JsonParseException | JsonValidationException e) {
			System.err.println("[deleteGameOnServer] Error server sent invalid Json");
			e.printStackTrace();
	
			throw new ServerNotAvailableException();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("[deleteGameOnServer] Deleted #" + gameId.get());
	}
	
	/**
	 * Checks whether every player is available
	 * @param players
	 */
	private void checkAvailability(ArrayList<Player> players) {
		
		System.out.println("[checkAvailability] Checking..");
		
		try 
		{
			for(Player p : players) {
				this.checkAvailabilityForPlayer(p);
			}
		} catch(PlayerNotAvailableException e) {
			System.err.printf("[checkAvailability] Failed, player %s(#%d) not available%n",
					e.getCausingPlayer().getName(),
					e.getCausingPlayer().getPlayerId().get());

			System.err.println("[checkAvailability] Aborting tournament..");
			System.exit(1);
		}
		
		System.out.printf("[checkAvailability] All %d players are available!%n", players.size());
	}
	
	
	static class GameStartMessageDummy {
		final String messageType = "start";
		int gameId = 0;
		int playerId = 0;
		int maxTurnTime = 0;
		BoardStruct board;
	}
	
	/**
	 * Checks whether a player is available/reachable by sending a start message to the player
	 * @param player
	 * @throws PlayerNotAvailableException
	 */
	private void checkAvailabilityForPlayer(Player player) throws PlayerNotAvailableException {
		GameStartMessageDummy msg = new GameStartMessageDummy();
		
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		HttpPost post = new HttpPost(player.getUrl());
		post.setConfig(this.requestConfig);
		post.setHeader("Content-type", "application/json");
		post.setHeader("Connection", "close");

		try {
			Gson gson = new Gson();
			String msgStr = gson.toJson(msg);

			post.setEntity(new StringEntity(msgStr));

			HttpResponse response = client.execute(post);

			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				System.err.println("[checkAvailabilityForPlayer] Got invalid response");
				throw new PlayerNotAvailableException(player);
			}

		} catch (SocketTimeoutException e) {
			System.err.println("[checkAvailabilityForPlayer] Got a timeout");
			throw new PlayerNotAvailableException(player);
		} catch(HttpHostConnectException e) {
			System.err.println("[checkAvailabilityForPlayer] Failed to open connection");
			throw new PlayerNotAvailableException(player);
		} catch (IOException e) {
			System.err.println("[checkAvailabilityForPlayer] Got an IOException");
			e.printStackTrace();
			throw new PlayerNotAvailableException(player);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sets up the game availability array
	 */
	private void prepareAvailableGames() {
		this.gamesAvailable = new boolean[this.allPlayers.size()][this.allPlayers.size()];
		for (int i = 0; i < this.allPlayers.size(); i++) {
			for (int j = 0; j < this.allPlayers.size(); j++) {
				this.gamesAvailable[i][j] = i != j;
			}
		}
	}
	
	/**
	 * Moves all players into the available pool, does not call poolUpdate()
	 */
	private void prepareAvailablePool() {
		this.playerPool.clear();
		this.playerPool.addAll(this.allPlayers);
		System.out.println("[prepareAvailablePool] Prepared pool with " + this.playerPool.size() + " players");
	}
	
	/**
	 * Moves a player into the available pool if it still has games to play, calls poolUpdate() if necessary
	 * @param player
	 */
	private void addPlayerToPool(Player player) {
		if(this.isPlayerFinished(player)) {
			return;
		}
		
		this.playerPool.add(player);
		this.poolUpdate();
	}
	
	/**
	 * Returns whether a player still can participate in additional games
	 * @param player to be checked
	 * @return true if player cant play in any more games, false otherwise
	 */
	private boolean isPlayerFinished(Player player) {
		
		// run through every possible game combination
		for (int i = 0; i < this.gamesAvailable.length; i++) { // index and player ids are identical
			for (int j = 0; j < this.gamesAvailable[0].length; j++) {
				
				// atleast one of the players needs to be "player"
				if(i != player.getPlayerId().get() && j != player.getPlayerId().get()) { 
					continue; 
				}
				
				if (this.gamesAvailable[i][j]) {
					return false;
				}
			}
		}
		
		// if it gets here there is no game left for the player
		return true;
	}
	
	/**
	 * Restores games that need to be restored and then starts
	 */
	private void checkForRestoreAndStart() {
		ArrayList<Game> gamesToBeRestored = this.backupManager.restore();
		
		// sets up the game availability array
		this.prepareAvailableGames();
		
		// move all players into the available pool
		this.prepareAvailablePool();
			
		if(gamesToBeRestored != null) {
			System.out.println("[restoreFromFilesOnStartUp] Found " + gamesToBeRestored.size() + " games that need to be restored before we can resume");
			
			try {
				this.restoreFromHistory(gamesToBeRestored);
			} catch (ServerNotAvailableException e) {
				System.out.println("[restoreFromFilesOnStartUp] Server not available");
				
				this.serverHasDiedEvent();
				
				return;
			}
		} else {	
			System.out.println("[restoreFromFilesOnStartUp] Nothing to restore found..");
		}
		
		// in theory the tournament could be over already
		this.checkIfTournamentIsFinished();
		
		// starts creating games if possible (if the tournament is not over yet)
		this.poolUpdate();
	}
	
	/**
	 * Restores from the history and starts
	 * @param history
	 */
	private void restoreFromHistory(ArrayList<Game> history) throws ServerNotAvailableException {
		System.out.println("[restoreFromHistory] Restoring a history of " + history.size() + " games");
		
		this.prepareAvailableGames();
		this.prepareAvailablePool();
		
		Optional<Game> newestGame = history.stream().max((a, b) -> a.getGameId().get() - b.getGameId().get() );

		this.nextGameId = newestGame.map(game -> game.getGameId().get() + 1)
				.orElse(1);
		
		for(Game game : history) {			
			Player playerOne = game.getPlayer(GamePlayerId.PLAYER1);
			Player playerTwo = game.getPlayer(GamePlayerId.PLAYER2);
			
			this.gamesAvailable[playerOne.getPlayerId().get()][playerTwo.getPlayerId().get()] = false;
			
			this.createHistoryGameOnServer(game);
		}
		
		System.out.println("[restoreFromHistory] Successfully restored from history");		
	}
	
	/**
	 * Event getting called when server has died
	 */
	public synchronized void serverHasDiedEvent() {
		if(this.serverIsDead) return; // event is already getting handled
		this.serverIsDead = true;
		
		System.out.println("[serverHasDiedEvent] Got called");
		
		// in theory we could still use the data in here, gets cleared for now though 
		// TODO
		this.gameFinishedQueue.clear();
		
		// abort all threads and clear them
		this.runningWatchingThreads.forEach(GameWatchingThread::abort);
		this.runningWatchingThreads.clear();
		
		// try to restore the server
		while(this.serverIsDead) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			try {
				this.restoreServer();
			} catch(ServerNotAvailableException e) {
				System.out.println("[serverHasDiedEvent] Server not available, retrying after 5 seconds..");
			}			
		}
	}
	
	/**
	 * Restores the server if it had died but is now online again
	 */
	private void restoreServer() throws ServerNotAvailableException {
		this.prepareServer();
		
		this.restoreFromHistory(this.backupManager.getHistory());
		
		this.serverIsDead = false;
		
		System.out.println("[restoreServer] Resuming normal operations..");
		
		// in theory the tournament could be over already
		this.checkIfTournamentIsFinished();
		
		// starts creating games if possible (if the tournament is not over yet)
		this.poolUpdate();
	}
	
	/**
	 * This method gets called whenever a new player gets added into the pool to create new games with players from the pool whenever possible
	 */
	private void poolUpdate() {
		// cheap way to fix what happens if two threads finish their games at almost the same time
		if(this.poolUpdatingRunning || this.serverIsDead) return;
		this.poolUpdatingRunning = true;
		
		for (Player x : this.playerPool) {
			for (Player y : this.playerPool) {				
				if (this.gamesAvailable[x.getPlayerId().get()][y.getPlayerId().get()]) {
					// found available matchup, start game between player x and y
					
					this.startNewGame(x, y);
					
					this.removeFromPool(x, y);
					this.gamesAvailable[x.getPlayerId().get()][y.getPlayerId().get()] = false;

					// restart search if match was found
					this.poolUpdatingRunning = false;
					this.poolUpdate();
					return;
				}
			}
		}
		
		// stops if no match was found
		this.poolUpdatingRunning = false;
	}
	
	/**
	 * Removes two players from the pool of available players
	 * @param playerOne
	 * @param playerTwo
	 */
	private void removeFromPool(Player playerOne, Player playerTwo) {
		this.playerPool.removeIf(p -> (p.getPlayerId() == playerOne.getPlayerId() || p.getPlayerId() == playerTwo.getPlayerId()));
	}
	
	/**
	 * Creates a new game between these players on the game server and then creates a GameWatchingThread instance for it
	 * @param playerOne white player
	 * @param playerTwo black player
	 */
	private void startNewGame(Player playerOne, Player playerTwo) throws ServerNotAvailableException {
		Game gameToWatch = this.createGameOnServer(playerOne, playerTwo);
		GameWatchingThread newThread = new GameWatchingThread(this, gameToWatch, this.serverAddress, this.serverToken);
		this.runningWatchingThreads.add(newThread);
		
		newThread.start();
	}
	
	/**
	 * Create game Message
	 */
	static class CreateGameMessage {
		GameStruct game;
	}
	
	/**
	 * Creates a new game on the server
	 * @param playerOne white player
	 * @param playerTwo black player
	 * @return the game that just got created
	 */
	private Game createGameOnServer(Player playerOne, Player playerTwo) throws ServerNotAvailableException {
		Player[] players = {playerOne, playerTwo};
		Game newGame = new Game(this.maxTurnTime, this.getNextGameId(), players, this.startingBoard);
		
		CreateGameMessage msg = new CreateGameMessage();
		msg.game = GameStruct.fromModel(newGame, true);
		
		// do post request on server
		
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		String postUrl = String.format("%s/games/%d?token=%s",
				this.serverAddress,
				newGame.getGameId().get(),
				this.serverToken);

		HttpPost post = new HttpPost(postUrl);
		post.setConfig(this.requestConfig);
		post.setHeader("Content-type", "application/json");
		post.setHeader("Connection", "close");

		try {
			String msgStr = new Gson().toJson(msg);

			post.setEntity(new StringEntity(msgStr));

			HttpResponse resp = client.execute(post);

			if(resp.getStatusLine().getStatusCode() != 200) {
				System.err.println("[createGameOnServer] Invalid token provided, or server implements api incorrectly");
				System.exit(1);
				return null;
			}
		} catch (SocketTimeoutException e) {
			System.out.println("[createGameOnServer] Got a timeout");
			
			throw new ServerNotAvailableException();
		} catch (IOException e) {
			System.out.println("[createGameOnServer] Error");
			e.printStackTrace();
			
			throw new ServerNotAvailableException();
		} catch (JsonParseException | JsonValidationException e) {
			System.err.println("[createGameOnServer] Error server sent invalid Json");
			e.printStackTrace();
	
			throw new ServerNotAvailableException();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.printf("[createGameOnServer] Game #%d (%s vs. %s) got created%n",
				newGame.getGameId().get(),
				playerOne.getName(),
				playerTwo.getName());
		
		return newGame;
	}
	
	/**
	 * Creates a game on the server that already finished
	 * @param game
	 */
	private void createHistoryGameOnServer(Game game) throws ServerNotAvailableException{		
		CreateGameMessage msg = new CreateGameMessage();
		msg.game = GameStruct.fromModel(game, true);
		
		// do post request on server
		
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		String postUrl = String.format("%s/games/%d?token=%s",
				this.serverAddress,
				game.getGameId().get(),
				this.serverToken);

		HttpPost post = new HttpPost(postUrl);
		post.setConfig(this.requestConfig);
		post.setHeader("Content-type", "application/json");
		post.setHeader("Connection", "close");

		try {
			String msgStr = new Gson().toJson(msg);

			post.setEntity(new StringEntity(msgStr));

			HttpResponse resp = client.execute(post);

			if(resp.getStatusLine().getStatusCode() != 200) {
				System.out.println("[createGameOnServer] Invalid token provided, or server implements api incorrectly");
				System.exit(1);
			}
		} catch (SocketTimeoutException e) {
			System.out.println("[createHistoryGameOnServer] Got a timeout");
			
			throw new ServerNotAvailableException();
		} catch (IOException e) {
			System.out.println("[createHistoryGameOnServer] Error");
			e.printStackTrace();
			
			throw new ServerNotAvailableException();
		} catch (JsonParseException | JsonValidationException e) {
			System.err.println("[createHistoryGameOnServer] Error server sent invalid Json");
			e.printStackTrace();
	
			throw new ServerNotAvailableException();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Player playerOne = game.getPlayer(GamePlayerId.PLAYER1);
		Player playerTwo = game.getPlayer(GamePlayerId.PLAYER2);	
		
		System.out.println("[createHistoryGameOnServer] Restored game #" + game.getGameId().get() + " (" + playerOne.getName() + " vs. " + playerTwo.getName() + ")");
	}
	
	private GameId getNextGameId() {
		return new GameId(this.nextGameId++);
	}
	
	/**
	 * Callback method for the GameWatchingThread class, gets called upon game is finished
	 * @param gameWatchingThread
	 */
	public void addToGameIsFinishedQueue(GameWatchingThread gameWatchingThread) {
		this.gameFinishedQueue.add(gameWatchingThread);
		this.gameFinishedEvent();
	}
	
	/**
	 * Handles the event of a game being finished
	 */
	private void gameFinishedEvent() {
		assert !this.gameFinishedQueue.isEmpty();

		// cheap way to fix what happens if two threads finish their games at almost the same time
		if(this.gameFinishedQueueRunning || this.serverIsDead) return;
		this.gameFinishedQueueRunning = true;
		
		// retrieves first element and removes it from the queue
		GameWatchingThread gameWatchingThread = this.gameFinishedQueue.poll();
		Game game = gameWatchingThread.getGame();
		
		Player playerOne = game.getPlayer(GamePlayerId.PLAYER1);
		Player playerTwo = game.getPlayer(GamePlayerId.PLAYER2);
		
		System.out.println("[gameIsFinished] Game #" + game.getGameId().get() + " (" + playerOne.getName() + " vs. " + playerTwo.getName() + ") just finished!");
		
		// readd players to pool (if necessary)
		this.addPlayerToPool(playerOne);
		this.addPlayerToPool(playerTwo);
		
		// remove thread		
		this.runningWatchingThreads.removeIf(t -> t.getGame().getGameId() == gameWatchingThread.getGame().getGameId()); // maybe we can use a different id here
		
		// create a backup
		this.backupManager.storeGameInHistory(game);
		
		this.checkIfTournamentIsFinished();
		
		this.gameFinishedQueueRunning = false;
		
		// work on next finished game if one exists
		if(!this.gameFinishedQueue.isEmpty()) {
			this.gameFinishedEvent();
		}
	}
	
	/**
	 * Closes the tournament if it's finished
	 */
	private void checkIfTournamentIsFinished() {
		// no games are running atm and no players are available anymore
		
		if(this.runningWatchingThreads.size() != 0 || this.playerPool.size() != 0) {
			return;
		}
		
		System.out.println("Tournament is over!");
		
		try {
			this.backupManager.dumpBackup();
		} catch (IOException e) {
			System.out.println("[checkIfTournamentIsFinished] Failed to dump the backup");
			
			e.printStackTrace();
		}
	}
}
