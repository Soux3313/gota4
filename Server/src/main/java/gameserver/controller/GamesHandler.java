package gameserver.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import gameserver.Gameserver;
import gameserver.result.GameRemoveResult;
import gameserver.sync.LockGuard;
import gameserver.sync.OwningLock;
import model.game.Game;
import model.ids.GameId;
import model.jsonstruct.GameStruct;
import model.jsonstruct.ReducedGameStruct;
import org.apache.http.HttpStatus;
import validation.JsonRequireRecvRecursive;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The GameHandler class covers the {@link Gameserver} responses on the /players
 * route. Therefore this class implements methods for the request types GET,POST
 * and DELETE. Look at the {@link CommunicationHandler} class for more details.
 */
public class GamesHandler extends CommunicationHandler {

	/**
	 * The Pattern objects that will later be used for matching GET-requests.
	 */
	private static final Pattern GETpattern = Pattern
			.compile("/games((/(?<gameId>\\d+))|/)?(\\?token=(?<token>.+))?");

	/**
	 * The Pattern objects that will later be used for matching POST-requests.
	 */
	private static final Pattern POSTpattern = Pattern
			.compile("/games/(?<gameId>\\d+)\\?token=(?<token>.+)");

	/**
	 * The Pattern objects that will later be used for matching DELETE-requests.
	 */
	private static final Pattern DELETEpattern = Pattern
			.compile("/games/(?<gameId>\\d+)\\?token=(?<token>.+)");

	// The last two are redundant but the API might change, so I will leave them
	// here



	static class GetGamesResponse {
		ReducedGameStruct[] games;
	}

	static class GetGameResponse {
		GameStruct game;
	}

	/**
	 * This defines the structure of a requestBody in Json format
	 */
	static class PostRequestBody {
		@JsonRequireRecvRecursive
		GameStruct game;
	}

	/**
	 * <p>
	 * The constructor of the GamesHandler class. It takes an Gameserver object to
	 * which the GamesHandler communicates during requests. This class extends the
	 * CommunicationHandler class that coordinates this process.
	 * </p>
	 * <p>
	 * Also look at the {@link CommunicationHandler}
	 * </p>
	 * class.
	 * <p>
	 * This controller is based on the
	 * <a href="https://github.com/dice-group/Amazons/wiki/Game-Server-API">Dice
	 * Group Wiki</a>
	 * </p>
	 * 
	 * @param gameserver The Gameserver object this class can communicate during
	 *                   requests.
	 */
	public GamesHandler(Gameserver gameserver) {
		super(gameserver);
	}

	/**
	 * This method is invoked by the handle() method if the given HttpExchange
	 * object is of type GET.
	 * 
	 * @param exchange the actual HttpExchange object that holds the information of
	 *                 the request.
	 */
	private HttpResponse handleGET(HttpExchange exchange) {
		getLogger().trafficInboundRequest("GET", exchange.getRequestURI().getPath(), "");

		Matcher m = GETpattern.matcher(exchange.getRequestURI().toString());

		if (!m.matches()) {
			// Bad request
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Bad request route");
		}


		String token = m.group("token");
		String strGameId = m.group("gameId");
		boolean authenticated;

		if (token != null) {
			if (verifyToken(token)) {
				authenticated = true;
			} else {
				return makeResponse(HttpStatus.SC_UNAUTHORIZED, "invalid token provided");
			}
		} else {
			authenticated = false;
		}

		getLogger().debug("GET Authenticated?: " + authenticated);
		if (strGameId != null) { // gameId is provided
			// return specific game
			GameId gameId = new GameId(Integer.parseInt(strGameId));

			Optional<OwningLock<Game>> g = this.getGameserver().getGameByID(gameId);

			// Game doesn't exist:
			if (g.isEmpty()) {
				return makeResponse(HttpStatus.SC_BAD_REQUEST, "Game doesn't exist");
			}

			GetGameResponse response = new GetGameResponse();

			getLogger().debug("trying to get lock");
			try (LockGuard<Game> gs = g.get().lock()) {
				getLogger().debug("got lock in GET");
				response.game = GameStruct.fromModel(gs.get(), authenticated);
			}

			JsonElement responseJson = new Gson().toJsonTree(response);
			return makeResponse(HttpStatus.SC_OK, responseJson);
		} else {
			// return all games
			try {
				GetGamesResponse response = new GetGamesResponse();
				response.games = this.getGameserver()
						.getGames()
						.map(g -> {
							try (LockGuard<Game> gs = g.lock()) {
								return ReducedGameStruct.fromModel(gs.get(), authenticated);
							}
						})
						.toArray(ReducedGameStruct[]::new);

				JsonElement responseJson = new Gson().toJsonTree(response);
				return makeResponse(HttpStatus.SC_OK, responseJson);
			} catch (Exception e) {
				e.printStackTrace();
				return makeResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}

		}
	}

	/**
	 * This method is invoked by the handle() method if the given HttpExchange
	 * object is of type POST
	 * 
	 * @param exchange the actual HttpExchange object that holds the information of
	 *                 the request
	 */
	private HttpResponse handlePOST(HttpExchange exchange) {
		/*
		 * SO what did I do here? The wiki comes along an example-request-body.
		 * Strangely they put the token both in the url and in the body. Inconsistently
		 * they don't put the gameId in the Json-body but only in the url. In addition
		 * to that, playerIds just become id in the Json.
		 * 
		 * I changed the Struct-classes accordingly by removing the required gameId
		 * (that comes only from the url now). The token in the Json-body will be
		 * ignored. Further, in the request, it is !playerId! and not just id.
		 * 
		 * In the second example-player it is "ulrl" instead of "url".
		 * 
		 * The initalBoard attribute of the GameStruct does not exist in the example
		 * request. Its just "board" there.
		 * 
		 * It might be necessary to let the API designers examine these issues.
		 * 
		 * Besides all this weired problems. I managed to create a working test-request:
		 * This here is a working JSON-body:
		 * 
		 * { "game" : { "players" : [ { "playerId" : 0, "name" : "Example player 1",
		 * "url" :
		 * "https://www.example.com/v1/player?token=034128a523e3c10e678dd88ec33bdf5c" },
		 * { "playerId" : 1, "name" : "Example player 2", "url" :
		 * "https://www.example.com/v1/player?token=1d3b19db2e913abc003b7bd7e27fd9e7" }
		 * ], "maxTurnTime" : 60000, "initialBoard" : { "gameSizeRows" : 10,
		 * "gameSizeColumns" : 10, "squares" : [ [ -1, -1, -1, 1, -1, -1, 1, -1, -1,
		 * -1], [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1], [ -1, -1, -1, -1, -1, -1, -1,
		 * -1, -1, -1], [ 1, -1, -1, -1, -1, -1, -1, -1, -1, 1], [ -1, -1, -1, -1, -1,
		 * -1, -1, -1, -1, -1], [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1], [ 0, -1, -1,
		 * -1, -1, -1, -1, -1, -1, 0], [ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1], [ -1,
		 * -1, -1, -1, -1, -1, -1, -1, -1, -1], [ -1, -1, -1, 0, -1, -1, 0, -1, -1, -1]
		 * ] } } }
		 * 
		 * Notice: Adding a comma after the last 'squares' row will cause an error.
		 */

		// my result:
		Matcher m = POSTpattern.matcher(exchange.getRequestURI().toString());

		if (!m.matches()) {
			// Bad request
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Bad request route or missing token");
		}

		getLogger().debug("matched POST regex");

		// get Values from URL:
		int gameId = Integer.parseInt(m.group("gameId"));
		String token = m.group("token");

		if (token == null || !verifyToken(token)) {
			return makeResponse(HttpStatus.SC_UNAUTHORIZED, "Invalid token");
		}

		getLogger().debug("valid access token for POST");

		// All test passed. Now parse the body:
		JsonValidatingParser parser = new JsonValidatingParser();
		try {
			PostRequestBody requestBody = parser.fromJson(readBody(exchange), PostRequestBody.class);
			requestBody.game.gameId = gameId;

			// squares empty
			if (requestBody.game.initialBoard.squares.length == 0 || requestBody.game.initialBoard.squares[0].length == 0) {
				return makeResponse(HttpStatus.SC_BAD_REQUEST, "squares has empty dimension");
			}

			// squares sizes not same as gameSizes
			if (requestBody.game.initialBoard.gameSizeRows != requestBody.game.initialBoard.squares.length
					|| requestBody.game.initialBoard.gameSizeColumns != requestBody.game.initialBoard.squares[0].length) {

				return makeResponse(HttpStatus.SC_BAD_REQUEST, "gameSizes do not match with squares");
			}

			this.getGameserver().addGame(requestBody.game.intoModel());
			return makeResponse(HttpStatus.SC_OK, "Game added");

		} catch (JsonParseException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Syntax error: " + e.getMessage());
		} catch (JsonValidationException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "validation error: " + e.getMessage());
		} catch (Exception e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage());
		}
	}

	/**
	 * This method is invoked by the handle() method if the given HttpExchange
	 * object is of type DELETE
	 * 
	 * @param exchange the actual HttpExchange object that holds the information of
	 *                 the request
	 */
	private HttpResponse handleDELETE(HttpExchange exchange) {
		getLogger().trafficInboundRequest("DELETE", exchange.getRequestURI().getPath(), "");

		Matcher m = DELETEpattern.matcher(exchange.getRequestURI().toString());

		// Bad request or missing token
		if (!m.matches()) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Bad request route or missing token");
		}

		// get Values from URL:
		GameId gameId = new GameId(Integer.parseInt(m.group("gameId")));
		String maybeToken = m.group("token");

		if (maybeToken == null || !verifyToken(maybeToken)) {
			return makeResponse(HttpStatus.SC_UNAUTHORIZED, "Invalid token");
		}

		getLogger().debug("valid token in delete");

		// game does not exist
		if (!this.getGameserver().doesGameExistById(gameId)) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Game doesn't exist");
		}

		GameRemoveResult res = this.getGameserver().removeGame(gameId);
		switch (res) {
			case INTERRUPTED_AND_REMOVED:
				return makeResponse(HttpStatus.SC_OK, "game aborted and deleted");
			case REMOVED:
				return makeResponse(HttpStatus.SC_OK, "game deleted");
			default:
				throw new IllegalStateException("unreachable");
		}
	}

	@Override
	public HttpResponse serve(HttpExchange session) {
		switch (session.getRequestMethod()) {
			case "GET":
				return handleGET(session);
			case "POST":
				return handlePOST(session);
			case "DELETE":
				return handleDELETE(session);
			default:
				return makeResponse(HttpStatus.SC_BAD_REQUEST, "invalid method");
		}
	}

}
