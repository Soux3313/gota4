package aiplayer.controller;

import aiplayer.AIPlayer;
import com.google.gson.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.board.Board;
import model.exceptions.InvalidGamePlayerIdException;
import model.exceptions.UnsupportedPieceCodeException;
import model.ids.GameId;
import model.ids.GamePlayerId;
import model.jsonstruct.BoardStruct;
import model.jsonstruct.TurnStruct;
import model.player.Turn;
import validation.JsonRequireRecv;
import validation.JsonRequireRecvRecursive;
import validation.JsonValidatingParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to handle incoming http requests for the ai-player
 */
public class TurnRequestHandler implements HttpHandler {

	private final Map<GameId, Duration> maxTurnTimes = new HashMap<>();
	private final AIPlayer aiPlayer;

	public TurnRequestHandler(AIPlayer aiPlayer) {
		this.aiPlayer = aiPlayer;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
			exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
			exchange.sendResponseHeaders(204, -1);
			return;
		}
		if (exchange.getRequestMethod().equals("POST")) {
			this.handlePOST(exchange);
		} else {
			respond(400, "NO POST", exchange);
		}
	}
	
	// The following structs have been copied from Server.GameThread
	static class GameStartMessage {
		final String messageType = "start";
		Integer playerId;
		BoardStruct board;

		@JsonRequireRecv
		Integer gameId;

		@JsonRequireRecv
		Integer maxTurnTime;
	}

	static class TurnStartMessage {
		final String messageType = "turn";
		Integer turnId;
		TurnStruct enemyTurn;

		@JsonRequireRecv
		Integer playerId;

		@JsonRequireRecv
		Integer gameId;

		@JsonRequireRecvRecursive
		BoardStruct board;
	}

	static class TurnResponse {
		@JsonRequireRecvRecursive
		TurnStruct turn;
	}

	static class GameEndMessage {
		final String messageType = "end";
		Integer turnId;
		Integer playerId;
		Integer winningPlayer;

		@JsonRequireRecv
		Integer gameId;
	}

	/**
	 * Handles incoming POST request
	 * @param exchange the http session
	 */
	private void handlePOST(HttpExchange exchange) {

		JsonValidatingParser parser = new JsonValidatingParser();

		try {
			JsonObject parsedBody = parseRequestBody(exchange);

			switch (parsedBody.get("messageType").getAsString()) {
			case "start":
				GameStartMessage startMessage = parser.fromJson(parsedBody, GameStartMessage.class);	
				this.handleStartMessage(startMessage, exchange);
				return;
			case "turn":
				TurnStartMessage turnMessage = parser.fromJson(parsedBody, TurnStartMessage.class);
				this.handleTurnMessage(turnMessage, exchange);
				return;
			case "end":
				GameEndMessage endMessage = parser.fromJson(parsedBody, GameEndMessage.class);
				this.handleEndMessage(endMessage, exchange);
				return;
			default:
				respond(400, "invalid json received", exchange);
			}
		} catch (Exception e) {
			String msg = "Error while trying to parse incoming message: " + e.getClass().toString() + " " + e.getMessage();
			System.err.println(msg);
			respond(400, msg, exchange);
		}
	}
	
	/**
	 * Handles incoming start message
	 */
	private void handleStartMessage(GameStartMessage startMessage, HttpExchange exchange) {
		if (startMessage.maxTurnTime > 0) {
			this.maxTurnTimes.put(new GameId(startMessage.gameId), Duration.ofMillis(startMessage.maxTurnTime));
			respond(200, "", exchange);
		} else {
			System.out.println("ignoring incoming game because of maxTurnTime <= 0");
			// have to respond 200 because of API
			respond(200, "cannot initiate a game with maxTurnTime <= 0, ignoring", exchange);
		}
	}
	
	/**
	 * Handles incoming request for a turn and responds with it
	 */
	private void handleTurnMessage(TurnStartMessage turnMessage, HttpExchange exchange) {
		Board incomingBoard;
		try {
			incomingBoard = turnMessage.board.intoModel();
		} catch (UnsupportedPieceCodeException e) {
			String msg = "Error while trying to parse incoming board: " + e.getMessage();
			System.err.println(msg);
			respond(400 /*bad request*/, msg, exchange);
			return;
		}

		GamePlayerId playerId;
		try {
			playerId = GamePlayerId.fromInt(turnMessage.playerId);
		} catch (InvalidGamePlayerIdException e) {
			String msg = "Error: invalid playerId in request; expected 0 or 1, but got " + turnMessage.playerId;
			System.err.println(msg);
			respond(400 /*bad request*/, msg, exchange);
			return;
		}

		Duration maxTurnTime = this.maxTurnTimes.get(new GameId(turnMessage.gameId));
		if (maxTurnTime == null) {
			respond(400 /*bad request*/, "Error: received turn message before start message", exchange);
			return;
		}

		Turn responseTurn = this.aiPlayer.bestTurn(incomingBoard, playerId, maxTurnTime);
		TurnStruct responseTurnStruct = TurnStruct.fromModel(responseTurn);

		TurnResponse msg = new TurnResponse();
		msg.turn = responseTurnStruct;

		JsonElement msgObj = new Gson().toJsonTree(msg);
		respond(200, msgObj, exchange);
	}
	
	/**
	 * Handles incoming end message
	 */
	private void handleEndMessage(GameEndMessage endMessage, HttpExchange exchange) {
		this.maxTurnTimes.remove(new GameId(endMessage.gameId));
		respond(200, "", exchange);
	}


	/**
	 * This method is used to parse the request body of an incoming HttpRequest
	 * Notice: This is identical to the method parseRequestBody  Server.CommunicationHandler
	 * @param exchange the incoming HttpRequest
	 * @return a JsonObject containing the data from the request body
	 */
	protected JsonObject parseRequestBody(HttpExchange exchange) throws JsonParseException {
		InputStream in = exchange.getRequestBody();
		try {
			String content = new String(in.readAllBytes());
			return JsonParser.parseString(content).getAsJsonObject();
		} catch (IOException | JsonParseException e) {
			throw new JsonParseException(e.getMessage());
		}
	}

	/**
	 * Method used to respond on incoming HttpRequests
	 *
	 * @param code     the statuscode to send to the client
	 * @param answer   the answer to send to the client as a JsonObject,
	 *                    if this is instanceof String the raw string will be send
	 * @param exchange the request on which to answer
	 */
	public <T> void respond(int code, T answer, HttpExchange exchange) {
		Gson gson = new Gson();

		try (OutputStream out = exchange.getResponseBody()) {

			Headers h = exchange.getResponseHeaders();
			h.add("Connection", "close");
			h.add("Access-Control-Allow-Origin","*");
			h.add("Access-Control-Allow-Methods","*");

			byte[] b;

			if (answer instanceof String) {
				b = ((String) answer).getBytes();
				h.add("Content-Type", "text/plain");
			} else {
				String json = gson.toJson(answer);
				b = json.getBytes();
				h.add("Content-Type", "application/json");
			}

			exchange.sendResponseHeaders(code, b.length);
			out.write(b);
		} catch (IOException e) {
			System.err.println("Error while trying to send a response: " + e.getMessage());
		}
	}


}
