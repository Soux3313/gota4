package tournamentmanager;

import com.google.gson.JsonParseException;
import https.HttpClientFactory;
import model.game.Game;
import model.game.GameState;
import model.ids.GamePlayerId;
import model.jsonstruct.GameStruct;
import model.jsonstruct.GetGameResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import tournamentmanager.exceptions.ServerNotAvailableException;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Class used to watch games for the tournament manager
 */
public class GameWatchingThread extends Thread {
	// server address to connect to
	private final String serverAddress;

	// token to be used in requests
	private final String serverToken;
	
	private final TournamentManager tManager;
	private final Game gameToWatch;
	
	// boolean indicating whether this thread should abort
	private volatile boolean abort = false;
	
	public GameWatchingThread(TournamentManager tManager, Game gameToWatch, String serverAddress, String serverToken) {
		this.tManager = tManager;
		this.gameToWatch = gameToWatch;
		
		this.serverAddress = serverAddress;
		this.serverToken = serverToken;
	}
	
	public Game getGame() {
		return this.gameToWatch;
	}
	
	/**
	 * Checks the status of the game at a certain interval, once the game is finished it reports back to the tournament manager and stops
	 */
	public void run() {
		while (this.gameToWatch.getGameState() != GameState.FINISHED && !this.abort) {
			
			try {
				this.update();
			} catch(ServerNotAvailableException e) {
				System.out.printf("[run-%d] Server not available%n", this.gameToWatch.getGameId().get());
				this.tManager.serverHasDiedEvent();
				return;
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// reinterrupt so we don't loose interrupt state
				currentThread().interrupt();
				return;
			}
		}
	}
	
	/**
	 * Reload data from Server
	 */
	private void update() throws ServerNotAvailableException {
		CloseableHttpClient client = HttpClientFactory.makeHttpsClientOrDie();

		String getUrl = String.format("%s/games/%d?token=%s",
				this.serverAddress,
				this.gameToWatch.getGameId().get(),
				this.serverToken);

		HttpGet get = new HttpGet(getUrl);
		
		get.setConfig(this.tManager.getRequestConfig());
		
		JsonValidatingParser parser = new JsonValidatingParser();
		try {
			HttpResponse response = client.execute(get);

			String body = new String(response.getEntity().getContent().readAllBytes());
			
			if(response.getStatusLine().getStatusCode() != 200) {
				System.out.printf("[update-%d] Invalid token provided, or server implements api incorrectly%n",
						this.gameToWatch.getGameId().get());
				System.exit(1);
				return;
			}
			
			GetGameResponse parsedResponse = parser.fromJson(body, GetGameResponse.class);
			GameStruct parsedGameStruct = parsedResponse.game;
			
			//check if the game is over
			if(parsedGameStruct.winningPlayer != null) {	
				if(parsedGameStruct.winningPlayer != 0 && parsedGameStruct.winningPlayer != 1) {
					System.out.printf("[update-%d] Got invalid winningplayer%n", this.gameToWatch.getGameId().get());
					
					throw new ServerNotAvailableException();
				}
				this.gameToWatch.finish(GamePlayerId.fromInt(parsedGameStruct.winningPlayer));
				this.finish();
			}
		} catch (SocketTimeoutException e) {
			System.err.printf("[update-%d] Got a timeout%n", this.gameToWatch.getGameId().get());
			
			throw new ServerNotAvailableException();
		} catch (IOException e) {
			System.err.printf("[update-%d] Error%n", this.gameToWatch.getGameId().get());
			e.printStackTrace();
			
			throw new ServerNotAvailableException();
		} catch (JsonParseException | JsonValidationException e) {
			System.err.printf("[update-%d] Error server sent invalid Json%n", this.gameToWatch.getGameId().get());
			e.printStackTrace();

			throw new ServerNotAvailableException();
		} finally {
			try {
				client.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
	}

	/**
	 * Aborts the update process and thus closes this thread
	 */
	public void abort() {
		this.abort = true;
	}
	
	/**
	 * Gets called when the game just finished, reports back to tournament manager
	 */
	private void finish() {
		if(!this.abort) {
			this.tManager.addToGameIsFinishedQueue(this);
		}
	}
}
