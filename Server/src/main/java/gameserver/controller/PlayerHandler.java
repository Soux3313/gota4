package gameserver.controller;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import gameserver.Gameserver;
import gameserver.result.PlayerRemoveResult;
import model.exceptions.PlayerIdAlreadyInUseException;
import model.ids.GlobalPlayerId;
import model.player.Player;
import org.apache.http.HttpStatus;
import validation.JsonRequireRecv;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * The PlayerHandler class covers the {@link Gameserver} responses on the
 * /players route. Therefore this class implements methods for the request types
 * GET,POST and DELETE. Look at the {@link CommunicationHandler} class for more
 * details.
 */
public class PlayerHandler extends CommunicationHandler {

	/**
	 * The Pattern objects that will later be used for matching POST-requests.
	 */
	private static final Pattern POSTpattern = Pattern.compile("/players/(\\d+)\\?token=(.+)");

	/**
	 * The Pattern objects that will later be used for matching DELETE-requests.
	 */
	private static final Pattern DELETEpattern = Pattern.compile("/players/(\\d+)\\?token=(.+)");

	// Those are redundant but the API might change, so I will leave them here.

	/**
	 * <p>
	 * The constructor of the PlayerHandler class. It takes an Gameserver object to
	 * which the PlayerHandler communicates during requests. This class extends the
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
	public PlayerHandler(Gameserver gameserver) {
		super(gameserver);
	}

	@Override
	public HttpResponse serve(HttpExchange session) {
		switch (session.getRequestMethod()) {
			case "POST":
				return this.handlePOST(session);
			case "DELETE":
				return this.handleDELETE(session);
			default:
				return makeResponse(HttpStatus.SC_BAD_REQUEST, "invalid method " + session.getRequestMethod());
		}
	}

	/**
	 * This class describes the structure of the expected body of POST-requests
	 */
	static class PostRequestBody {
		@JsonRequireRecv
		String name;
		@JsonRequireRecv
		String url;
	}


	/**
	 * This method is invoked by the handle() method if the given HttpExchange
	 * object is of type POST
	 * 
	 * @param exchange the actual HttpExchange object that holds the information of
	 *                 the request
	 */
	private HttpResponse handlePOST(HttpExchange exchange) {
		JsonValidatingParser parser = new JsonValidatingParser();

		// Does the URI math the expected pattern?
		Matcher m = POSTpattern.matcher(exchange.getRequestURI().toString());

		// invalid request route
		if (!m.matches()) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Bad request path.");
		}

		try {
			// Try parsing the request:
			PostRequestBody requestBody = parser.fromJson(readBody(exchange), PostRequestBody.class);

			// as the URI matches the pattern, this will always be given:
			GlobalPlayerId id = new GlobalPlayerId(Integer.parseInt(m.group(1)));
			String maybeToken = m.group(2);

			if (maybeToken == null || !verifyToken(maybeToken)) {
				return makeResponse(HttpStatus.SC_UNAUTHORIZED, "Invalid token");
			}

			Player player = new Player(id, requestBody.name, requestBody.url);
			this.getGameserver().addPlayer(player);

			return makeResponse(HttpStatus.SC_OK, "");
		} catch (JsonParseException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Parsing error: " + e.getMessage());
		} catch (JsonValidationException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Validation error: " + e.getMessage());
		} catch (PlayerIdAlreadyInUseException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage() + " Given Id: " + e.getReferencedPlayer().getPlayerId());
		} catch (Exception e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Misc error: " + e.getMessage());
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
		getLogger().trafficInboundRequest(
				"DELETE",
				exchange.getRequestURI().getPath(),
				"");

		// Does the URI math the expected pattern?
		Matcher m = DELETEpattern.matcher(exchange.getRequestURI().toString());

		// invalid request route
		if (!m.matches()) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Bad request path or missing token");
		}

		try {
			// Try parsing the request
			// as the URI matches the pattern, this will always be given:
			GlobalPlayerId playerId = new GlobalPlayerId(Integer.parseInt(m.group(1)));
			String maybeToken = m.group(2);

			if (maybeToken == null || !this.verifyToken(maybeToken)) {
				return makeResponse(HttpStatus.SC_UNAUTHORIZED, "invalid token");
			}

			if (!this.getGameserver().doesPlayerExistById(playerId)) {
				return makeResponse(HttpStatus.SC_BAD_REQUEST, "Player doesnt exist.");
			}

			PlayerRemoveResult res = this.getGameserver().removePlayer(playerId);

			switch (res) {
				case REMOVED:
					return makeResponse(HttpStatus.SC_OK, "Removed all Players with the id:" + playerId);
				case CANNOT_REMOVE_IN_GAME:
					return makeResponse(HttpStatus.SC_BAD_REQUEST, "cannot remove, player in game");
				default:
					throw new IllegalStateException("unreachable");
			}
		} catch (JsonParseException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Syntax Error: " + e.getMessage());
		} catch (JsonValidationException e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, "Validation Error: " + e.getMessage());
		} catch (Exception e) {
			return makeResponse(HttpStatus.SC_BAD_REQUEST, e.getMessage());
		}
	}
}
