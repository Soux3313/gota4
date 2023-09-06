package observer.network;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonParseException;

import https.HttpClientFactory;
import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.jsonstruct.GameStruct;
import model.jsonstruct.GetGameResponse;
import model.player.Player;
import model.player.Turn;
import observer.exceptions.FetchGameException;
import observer.exceptions.UnexpectedServerResponseException;
import observer.model.BoardHistory;
import observer.view.utils.JTextPaneGame;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

public class RemoteGame {

	private BoardHistory intermediateBoard;
    private final RemoteServer remoteServer;
    private final GameId gameId;

    /**
     * Determines wether the game is visible in the main GUI or not
     */
    private boolean isActive = false;

    /**
     * Game still running?
     */
    private boolean gameOver = false;

    /**
     * Representative in the GUI
     */
    private final JTextPaneGame gameButton;

    /**
     *
     */
    private final String gameInfo;

    private Player player1;
    private Player player2;

    private Optional<String> winningPlayerName = Optional.empty();
    private Optional<Integer> winningPlayerId = Optional.empty();

    public RemoteGame(RemoteServer server, int gameId, String gameInfo) throws FetchGameException, UnexpectedServerResponseException {
        this.remoteServer = server;
        this.gameId = new GameId(gameId);
        this.gameInfo = gameInfo;
        this.gameButton = new JTextPaneGame(this);

        //try to get the init board position to setup intermediateBoard:
        CloseableHttpClient client;
        try {
            client = HttpClientFactory.getNewHttpsClient();
        } catch (Exception e) {
            System.out.println("Critical failure, HttpClientFactory failed to construct new http client");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        HttpGet get = new HttpGet(this.getRequestURI());
        JsonValidatingParser parser = new JsonValidatingParser();
        try {
            HttpResponse response = client.execute(get);
            String body = new String(response.getEntity().getContent().readAllBytes());
            GetGameResponse parsedResponse = parser.fromJson(body, GetGameResponse.class);
            GameStruct parsedGameStruct = parsedResponse.game;
            this.intermediateBoard = new BoardHistory(parsedGameStruct.initialBoard.intoModel());

			player1 = parsedGameStruct.players[0].intoModel();
			player2 = parsedGameStruct.players[1].intoModel();

			if(parsedGameStruct.winningPlayer != null) {
				winningPlayerId = Optional.of(parsedGameStruct.winningPlayer);
				this.getGameButton().setWinningPlayerId(this.winningPlayerId.get());
				winningPlayerName = Optional.of(parsedGameStruct.players[parsedGameStruct.winningPlayer].name);
			}
        } catch (JsonParseException | JsonValidationException e) {
            System.err.println("The validation of the server response failed.");
            //TODO why does this exception exist? Isn't this like a FetchGameException as fetching failed?
            throw new UnexpectedServerResponseException(e.getMessage());
        } catch (IOException c) {
            throw new FetchGameException(get);
        } catch (UnsupportedPieceCodeException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch(IOException i) {
                i.printStackTrace();
            }
        }
    }

    /**
     * Reload data from Server
     */
    public void update() throws UnexpectedServerResponseException, FetchGameException {
    	if(!gameOver) {
    		CloseableHttpClient client;
    		try {
    			client = HttpClientFactory.getNewHttpsClient();
    		} catch (Exception e) {
    			System.out.println("Critical failure, HttpClientFactory failed to construct new http client");
    			e.printStackTrace();
			
    			System.exit(1);
    			return;
    		}
		
    		HttpGet get = new HttpGet(this.getRequestURI());
    		JsonValidatingParser parser = new JsonValidatingParser();
    		try {
    			HttpResponse response = client.execute(get);
    			String body = new String(response.getEntity().getContent().readAllBytes());
    			GetGameResponse parsedResponse = parser.fromJson(body, GetGameResponse.class);
    			GameStruct parsedGameStruct = parsedResponse.game;

    			//check if new turns have been found. If so, add them to intermediateBoard

				AtomicReference<GamePlayerId> pid = new AtomicReference<>(GamePlayerId.PLAYER1);
    			Stream<Turn> newTurns = Arrays.stream(parsedGameStruct.turns)
						.map(t -> t.intoModel(pid.getAndUpdate(GamePlayerId::other)));

    			Stream<Turn> prevTurns = intermediateBoard.getAllTurnsAsStream();

    			newTurns
					.skip(prevTurns.count())
					.forEachOrdered(intermediateBoard::addTurn);

				//check if the game is over
				if(parsedGameStruct.winningPlayer != null) {
					gameOver = true;

					winningPlayerId = Optional.of(parsedGameStruct.winningPlayer);
					this.getGameButton().setWinningPlayerId(this.winningPlayerId.get());
					winningPlayerName = Optional.of(parsedGameStruct.players[parsedGameStruct.winningPlayer].name);
				}

    		} catch (JsonParseException | JsonValidationException e) {
        		System.err.println("The validation of the server response failed.");
            	//TODO why does this exception exist? Isn't this like a FetchGameException as fetching failed?
            	throw new UnexpectedServerResponseException(e.getMessage());
        	} catch (IOException c) {
            	throw new FetchGameException(get);
        	} catch (UnsupportedPieceCodeException e) {
            	e.printStackTrace();
        	} finally {
            	try {
                	client.close();
            	} catch(IOException i) {
                	i.printStackTrace();
            	}
        	}
    	}
    }

    public boolean equals(RemoteGame remoteGame) {
        //return this.gameId.get() == remoteGame.gameId.get();
        return this.getRemoteServer().equals(remoteGame.getRemoteServer()) && this.gameId.equals(remoteGame.gameId);
    }

    public URI getRequestURI() {
		return this.remoteServer
				.getRequestURI()
				.resolve(String.format("/games/%s?token=%s",
						gameId.toString(),
						this.remoteServer.getConnection().getToken()));
    }
    
    public RemoteServer getRemoteServer() {
    	return this.remoteServer;
    }

    public GameId getGameId() {
        return gameId;
    }

    public JTextPaneGame getGameButton() {
        return gameButton;
    }

    public BoardHistory getIntermediateBoard() {
        return intermediateBoard;
    }

    public boolean isGameOver() {
        return gameOver;
    }
    
    public void setGameOver(boolean gameOver) {
    	this.gameOver = gameOver;
    }

    @Override
    public String toString() {
        return "Auf " + remoteServer.toString() + " spielen " + gameInfo;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public Optional<String> getWinningPlayerName() {
        return winningPlayerName;
    }

    public void setWinningPlayerId(int id) {
    	this.winningPlayerId = Optional.of(id);
    }
    
    public Optional<Integer> getWinningPlayerId() {
        return winningPlayerId;
    }

    public Player getPlayer1() {
    	return this.player1;
    }
   
    public Player getPlayer2() {
    	return this.player2;
    }

    public Player[] getPlayers() {
    	return new Player[] {this.player1, this.player2};
    }
}
