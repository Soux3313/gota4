package gameserver;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import gameserver.exceptions.*;
import gameserver.logger.Logger;
import gameserver.sync.LockGuard;
import gameserver.sync.OwningLock;
import https.HttpClientFactory;
import model.exceptions.InvalidTurnException;
import model.game.Game;
import model.game.GameState;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.ids.GlobalPlayerId;
import model.jsonstruct.BoardStruct;
import model.jsonstruct.PlayerStruct;
import model.jsonstruct.TurnStruct;
import model.player.Turn;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import validation.JsonRequireRecvRecursive;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * this thread orchistrates the sequence of operations
 * that need to take place for a game to be played
 * so it communicates with the players to advance the game state
 */
public class GameThread extends Thread {
	private final GameId gameToHandle;
	private final RequestConfig requestConfig;
	private final Gameserver parentServer;

	public GameThread(GameId gameToHandle, Gameserver parentServer) {
		this.parentServer = parentServer;
		this.gameToHandle = gameToHandle;

		try (LockGuard<Game> g = this.getGame().lock()) {
			long millis = g.get().getMaxTurnTime().toMillis();

			this.requestConfig = RequestConfig.custom()
					.setConnectTimeout(5000) // timeout till connection attempt gets cancelled
					.setSocketTimeout((int) millis) // time the aiplayer has until he has to finish his reponse (in theory this is only the maximum time of inactivity between two data packets)
					.setConnectionRequestTimeout(5000) // internal timeout for the library
					.build();
		}
	}

	/**
	 * retrieves the game which is handles by this thread from the gameserver
	 * @return the game protected by a lock
	 */
	public OwningLock<Game> getGame() {
		return this.parentServer
				.getGameByID(this.gameToHandle)
				.orElseThrow(() -> new IllegalStateException("gamethread still running while game was removed"));
	}

	public Logger getLogger() {
		return this.parentServer.getLogger();
	}

	/**
	 * schedules a task that cancels a request which should be aborted when the timeout expires
	 *
	 * @param t the timer to schedule the task on
	 * @param timeout the amount of time to the cancellation
	 * @param playerId the global player id of the player whos request got cancelled
	 * @param req the request to be cancelled
	 */
	private void scheduleRequestAbort(Timer t, Duration timeout, int playerId, HttpUriRequest req) {
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if (!req.isAborted()) {
					getLogger().info("player %d failed to respond in time, aborting request..", playerId);
					req.abort();
				}
			}
		};
		t.schedule(tt, timeout.toMillis());
	}

	static class GameStartMessage {
		final String messageType = "start";
		int gameId;
		int playerId;
		long maxTurnTime;
		BoardStruct board;

		public static GameStartMessage fromModel(Game g) {
			GameStartMessage ret = new GameStartMessage();
			ret.gameId = g.getGameId().get();
			ret.maxTurnTime = g.getMaxTurnTime().toMillis();
			ret.board = BoardStruct.fromModel(g.getBoard());

			return ret;
		}
	}

	static class TurnStartMessage {
		final String messageType = "turn";
		int gameId;
		int turnId;
		int playerId;
		BoardStruct board;
		TurnStruct enemyTurn;

		public static TurnStartMessage fromModel(Game g, GamePlayerId pid) {
			TurnStartMessage self = new TurnStartMessage();
			self.gameId = g.getGameId().get();
			self.turnId = g.getBoard().getNextTurnId().get();
			self.playerId = pid.get();
			self.board = BoardStruct.fromModel(g.getBoard());
			self.enemyTurn = g.getBoard().getLastTurn()
					.map(TurnStruct::fromModel)
					.orElse(null);

			return self;
		}
	}

	static class TurnResponse {
		@JsonRequireRecvRecursive
		TurnStruct turn;
	}

	static class GameEndMessage {
		final String messageType = "end";
		int gameId;
		int turnId;
		int playerId;
		Integer winningPlayer;

		public static GameEndMessage fromModel(Game g) {
			GameEndMessage self = new GameEndMessage();
			self.gameId = g.getGameId().get();
			self.turnId = g.getBoard().getNextTurnId().get();
			self.winningPlayer = g.getWinningPlayer()
					.map(GamePlayerId::get)
					.orElse(null);

			return self;
		}
	}

	/**
	 * Gets called right after the GameThread instance gets initialized, Game is now
	 * in GameState.STARTING
	 *
	 * This is the gameloop so to speak where
	 * Players get queried for their turns receive start and end messages and so on.
	 *
	 * in some cases this function 'consumes ownership' of a LockGuard in a try-with-resource block
	 * this is technically semantically incorrect in java since there is no way to express
	 * proper ownership, but it works regardless since i added a special case for it in the implementation
	 */
	@Override
	public void run() {
		// Send start message
		try {
			this.sendStartMessages(this.getGame().lock());
		} catch (InterruptedException e) {
			this.abortGame();
			currentThread().interrupt();
			return;
		} catch (PlayerTimeoutException e) {
			getLogger().info("One of the players failed to reply to the start message within a certain time, aborting game %d.",
					gameToHandle.get());

			this.abortGame();
			return;
		} catch (PlayerBehaviourException e) {
			e.printStackTrace();
			getLogger().info("One of the players showed an unexpected reaction to the start message, aborting game %d.",
					gameToHandle.get());

			this.abortGame();
			return;
		}

		// lock again because sendStartMessage unlocks midway through
		// and consumes ownership of the lock
		try (LockGuard<Game> g = this.getGame().lock()) {
			g.get().turnPlayerOne();
		}

		// game loop
		GamePlayerId currentPlayerId = GamePlayerId.PLAYER1;

		while (true) {

			Turn turn;
			try (LockGuard<Game> g = this.getGame().lock()) {
				currentPlayerId = g.get().getCurrentPlayerId()
						.orElseThrow();

				if (!g.get().getBoard().isTurnPossible(currentPlayerId)) {
					break;
				}

				// ask player for turn, if no turn was given let other player win
				// take ownership of g
				turn = this.askPlayerForTurn(g, currentPlayerId);
			} catch (InterruptedException e) {
				this.abortGame();
				currentThread().interrupt();
				return;
			} catch (PlayerBehaviourException e) {
				// currentPlayer will automatically loose
				// this can be achieved with a simple break, because once we exit the while loop
				// the other player automatically wins

				getLogger().info(
						"Player either failed to respond in time or caused or showed unexpected behavior and therefore has been disqualified.");
				break;
			}

			// check if game is won by checking if the next player can make any move, switch
			// to next player first
			// lock again because `askPlayerForTurn` unlocks midway through
			// and consumes ownership of the lock
			try (LockGuard<Game> g = this.getGame().lock()) {
				g.get().getBoard().applyTurn(turn);

				if (g.get().getGameState() == GameState.TURN_PLAYER1) {
					g.get().turnPlayerTwo();
				} else {
					g.get().turnPlayerOne();
				}
			} catch (InvalidTurnException e) {
				// turn was invalid
				// currentPlayer will automatically loose
				// this can be achieved with a simple break, because once we exit the while loop
				// the other player automatically wins

				e.printStackTrace();
				break;
			}
		}

		// set winner and finish, if it's the turn of playerOne playerTwo wins and vice
		// versa

		GamePlayerId winningPlayer = currentPlayerId.other();

		try (LockGuard<Game> g = this.getGame().lock()) {
			g.get().finish(winningPlayer);

			// take ownership of g
			this.sendEndMessages(g);
		} catch (PlayerBehaviourException e) {
			getLogger().info("One of the players showed an unexpected reaction to the end message");
		}
	}

	/**
	 * Aborts game immediately
	 */
	private void abortGame() {
		try (LockGuard<Game> g = this.getGame().lock()) {
			g.get().abort();
			try {
				this.sendEndMessages(g);
			} catch (PlayerBehaviourException e) {
				// ignore
			}
		}
	}

	/**
	 * sends a turn request (see wiki) to the specified player and awaits their response
	 * to then validate it and make a {@link model.player.Turn} out of it.
	 *
	 * @param pid the GamePlayerId of the player being asked
	 * @return the turn that the player generated
	 * @throws PlayerBehaviourException if the player did not respond according to spec
	 * 		(e.g. invalid turn, invalid json, timeout, etc.)
	 */
	private Turn askPlayerForTurn(LockGuard<Game> g, GamePlayerId pid) throws InterruptedException, PlayerBehaviourException {
		PlayerStruct p = PlayerStruct.fromModel(g.get().getPlayer(pid), true);
		TurnStartMessage msg = TurnStartMessage.fromModel(g.get(), pid);
		
		Duration maxTurnTime = g.get().getMaxTurnTime();

		// close the lockguard since the inner values are no longer referenced
		g.close();

		if (currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();
		HttpPost post = new HttpPost(p.url);
		post.setConfig(this.requestConfig);
		post.setHeader("Content-type", "application/json");

		// abort timer to cancel request if it takes to long
		Timer abortTimer = new Timer(true);

		try {
			String msgStr = new Gson().toJson(msg);

			getLogger().trafficOutboundRequest("POST",
					p.url,
					msgStr);

			post.setEntity(new StringEntity(msgStr));

			// schedule an abort if the request takes too long
			this.scheduleRequestAbort(abortTimer, maxTurnTime, p.playerId, post);

			HttpResponse resp = client.execute(post);
			InputStream is = resp.getEntity().getContent();
			String respStr = new String(is.readAllBytes());

			getLogger().trafficInboundResponse(
					resp.getStatusLine().getStatusCode(),
					respStr);
			
			if (currentThread().isInterrupted()) {
				throw new InterruptedException();
			}

			JsonValidatingParser parser = new JsonValidatingParser();
			return parser.fromJson(respStr, TurnResponse.class)
					.turn
					.intoModel(pid);
		} catch (InterruptedException e) {	
			throw e;
		} catch (SocketTimeoutException e) {	
			throw new PlayerTimeoutException(new GlobalPlayerId(p.playerId));
		} catch (JsonParseException | JsonValidationException e) {
			getLogger().err("player %d of game %d send invalid turn response; E: %s",
					p.playerId,
					gameToHandle.get(),
					e.getMessage());
			throw new MalformedPlayerResponseException(new GlobalPlayerId(p.playerId), "invalid turn response");
		} catch (IllegalArgumentException | IOException e) {
			getLogger().err("error while sending turn message to player %d of game %d; E: %s",
					p.playerId,
					gameToHandle.get(), e.getMessage());

			throw new PlayerReceiveException(new GlobalPlayerId(p.playerId), "unable to send turn message to player");
		} catch (Exception e) {
			throw new PlayerBehaviourException(new GlobalPlayerId(p.playerId), e.getMessage());
		} finally {
			abortTimer.cancel();
			
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Emits start messages to the players and waits for their response so the game
	 * can be started
	 *
	 * @throws PlayerBehaviourException if the player did not respond according to spec
	 * 		(e.g. status-code != 200 OK)
	 */
	private void sendStartMessages(LockGuard<Game> g) throws PlayerBehaviourException, InterruptedException {
		GameStartMessage msg = GameStartMessage.fromModel(g.get());

		// this is kind of a hack but i definitely want to free the lock before entering the
		// following loop and therefore i have to make sure no references to the players are used
		// inside the loop; Strings are fine since they are immutable
		PlayerStruct[] ps = Arrays.stream(g.get().getPlayers())
				.map(p -> PlayerStruct.fromModel(p, true))
				.toArray(PlayerStruct[]::new);

		// close the lockguard since the inner values are no longer referenced
		g.close();

		if (currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		for (int pid = 0; pid < ps.length; ++pid) {
			msg.playerId = pid;

			CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

			HttpPost post = new HttpPost(ps[pid].url);
			post.setConfig(this.requestConfig);
			post.setHeader("Content-type", "application/json");


			try {
				Gson gson = new Gson();
				String msgStr = gson.toJson(msg);

				getLogger().trafficOutboundRequest(
						"POST",
						ps[pid].url,
						msgStr);

				post.setEntity(new StringEntity(msgStr));

				HttpResponse response = client.execute(post);

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new PlayerResponseException(new GlobalPlayerId(ps[pid].playerId), "player did not respond or response was not 200 OK");
				}

				getLogger().trafficInboundResponse(
						response.getStatusLine().getStatusCode(),
						"");

			} catch (SocketTimeoutException e) {
				throw new PlayerTimeoutException(new GlobalPlayerId(ps[pid].playerId));
			} catch (IllegalArgumentException | IOException e) {
				getLogger().err("error while sending game start message to player %d of game %d; E: %s",
						ps[pid].playerId, gameToHandle.get(), e.getMessage());
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					getLogger().notice("could not close http client");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Emits end messages to the players
	 * 
	 * @throws PlayerBehaviourException if the player did not respond according to spec
	 * 		(e.g. status-code != 200 OK)
	 */
	private void sendEndMessages(LockGuard<Game> g) throws PlayerBehaviourException {
		assert g.get().getGameState() == GameState.FINISHED || g.get().getGameState() == GameState.ABORTED;

		GameEndMessage msg = GameEndMessage.fromModel(g.get());

		// this is kind of a hack but i definitely want to free the lock before entering the
		// following loop and therefore i have to make sure no references to the players are used
		// inside the loop; Strings are fine since they are immutable
		PlayerStruct[] ps = Arrays.stream(g.get().getPlayers())
				.map(p -> PlayerStruct.fromModel(p, true))
				.toArray(PlayerStruct[]::new);

		// close the lockguard since the inner values are no longer referenced
		g.close();

		for (int pid = 0; pid < ps.length; ++pid) {
			msg.playerId = pid;

			CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

			HttpPost post = new HttpPost(ps[pid].url);
			post.setConfig(this.requestConfig);
			post.setHeader("Content-type", "application/json");

			try {
				Gson gson = new Gson();
				String msgStr = gson.toJson(msg);

				getLogger().trafficOutboundRequest("POST",
						ps[pid].url,
						msgStr);

				post.setEntity(new StringEntity(msgStr));

				HttpResponse response = client.execute(post);

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new PlayerResponseException(new GlobalPlayerId(ps[pid].playerId), "player did not respond or response was not 200 OK");
				}

				getLogger().trafficInboundResponse(
						response.getStatusLine().getStatusCode(),
						"");

			} catch (IllegalArgumentException | IOException e) {
				getLogger().warning("error while sending game end message to player %d of game %d; E: %s",
						ps[pid].playerId, gameToHandle.get(), e.getMessage());
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					getLogger().notice("could not close http client");
					e.printStackTrace();
				}
			}
		}

	}
}
