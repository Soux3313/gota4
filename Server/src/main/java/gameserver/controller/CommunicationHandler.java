package gameserver.controller;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import gameserver.Gameserver;
import gameserver.logger.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Superclass for the Handlers on the Players and Games route
 */
public abstract class CommunicationHandler implements HttpHandler {

	/**
	 * The gameserver on which handlers of this type listen on
	 */
	private final Gameserver gameserver;

	/**
	 * Constructor of the CommunicationHandler class
	 * 
	 * @param gameserver The gameserver on which handlers of this type listen on
	 */
	public CommunicationHandler(Gameserver gameserver) {
		this.gameserver = gameserver;
	}

	protected Logger getLogger() {
		return this.gameserver.getLogger();
	}

	/**
	 * 
	 * @return The gameserver on which handlers of this type listen on
	 */
	protected Gameserver getGameserver() {
		return this.gameserver;
	}


	/**
	 * makes a {@link HttpResponse} from the provided data filling in the mimeType
	 * by checking the type of `answer`
	 * 		- answer instanceof String => text/plain
	 * 		- default => application/json
	 *
	 * @param status the http status to answer
	 * @param answer the response body
	 * @param <T> the generic type of the response body
	 * @return a valid {@link HttpResponse}
	 */
	public <T> HttpResponse makeResponse(int status, T answer) {

		if (answer instanceof String) {
			return new HttpResponse(status, "text/plain", (String) answer);
		} else {
			Gson gson = new Gson();
			String msg = gson.toJson(answer);

			return new HttpResponse(status, "application/json", msg);
		}
	}

	/**
	 * Verify the given token.
	 * 
	 * @param token the given token
	 * @return true, if the token is equal to the token of this server
	 */
	protected boolean verifyToken(String token) {
		return token.equals(gameserver.getOpts().token);
	}


	/**
	 * This method is used to parse the request body of an incoming HttpRequest
	 *
	 * @param exchange the incoming HttpRequest
	 * @return a JsonObject containing the data from the request body
	 */
	protected String readBody(HttpExchange exchange) throws IOException {
		try (InputStream in = exchange.getRequestBody()) {
			String ret = new String(in.readAllBytes());

			getLogger().trafficInboundRequest(
					exchange.getRequestMethod(),
					exchange.getRequestURI().getPath(),
					ret);

			return ret;
		}
	}


	/**
	 * implements httphandler functionality, this is intentionally final
	 * please override serve instead
	 */
	@Override
	public final void handle(HttpExchange exchange) {
		HttpResponse response = this.serve(exchange);

		try {
			getLogger().trafficOutboundResponse(
					response.getCode(),
					response.getBody());

			response.respond(exchange);
		} catch (IOException e) {
			getLogger().err("Error while trying to send a response: %s", e.getMessage());
		} finally {
			exchange.close();
		}
	}

	/**
	 * override this method to specify the behaviour of this handler
	 *
	 * @param session the session a request has spawned
	 * @return the response to that request
	 */
	public abstract HttpResponse serve(HttpExchange session);
}