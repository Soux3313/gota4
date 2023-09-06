package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.function.Predicate;


public class JsonValidationParserBenchmark {

	static class TokenValidator implements Predicate<Object> {
		public boolean test(Object obj) {
			return obj.equals("secret token");
		}
	}

	static class Player {
		@JsonRequireRecv String name;
		@JsonRequireRecv String url;
		@JsonRequireRecv Integer id;
	}

	static class Board {
		@JsonRequireRecvArray(sizes={2,2}) int[][] squares;
		@JsonRequireRecv Integer gameSizeRows;
		@JsonRequireRecv Integer gameSizeColumns;

	}


	static class Game {
		@JsonRequireRecvArray(sizes={2}) Player[] players;
		@JsonRequireRecvRecursive Board board;
		@JsonRequireRecv String token;
	}


	static Game game;

	@BeforeClass
	public static void setupGame() {
		Player player1 = new Player();
		player1.name = "P1";
		player1.url = "someURL";
		player1.id = 1;

		Player player2 = new Player();
		player2.name = "P2";
		player2.url = "someURL";
		player2.id = 2;

		Board board = new Board();
		board.gameSizeRows = 2;
		board.gameSizeColumns = 2;
		board.squares = new int[][] { new int[]{ 1, 2}, new int[]{ 3, 4 } };

		game = new Game();
		game.players = new Player[]{ player1, player2 };
		game.board = board;
		game.token = "secret token";
	}

	@Test
	public void manualTest() throws Exception {
		Gson gson = new Gson();
		JsonElement json = gson.toJsonTree(game, Game.class);

		for (int i = 0; i < 10000; ++i) {
			Game parsedGame = gson.fromJson(json, Game.class);

			if (parsedGame.token == null) {
				throw new Exception("");
			}
			TokenValidator v = new TokenValidator();
			if (!v.test(parsedGame.token)) {
				throw new Exception("");
			}


			if (parsedGame.board == null) {
				throw new Exception("");
			}
			if (parsedGame.board.squares == null) {
				throw new Exception("");
			}
			if (parsedGame.board.gameSizeColumns == null) {
				throw new Exception("");
			}
			if (parsedGame.board.gameSizeRows == null) {
				throw new Exception("");
			}
			if (parsedGame.board.squares.length != 2) {
				throw new Exception("");
			}

			for (int[] inner : parsedGame.board.squares) {
				if (inner.length != 2) {
					throw new Exception("");
				}
			}

			if (parsedGame.players == null) {
				throw new Exception("");
			}
			if (parsedGame.players.length != 2) {
				throw new Exception("");
			}

			for (Player p : parsedGame.players) {
				if (p == null) {
					throw new Exception("");
				}

				if (p.url == null) {
					throw new Exception("");
				}

				if (p.name == null) {
					throw new Exception("");
				}

				if (p.id == null) {
					throw new Exception("");
				}
			}
		}
	}


	@Test
	public void automaticTest() {

		Gson gson = new Gson();
		JsonElement json = gson.toJsonTree(game, Game.class);

		JsonValidatingParser vparser = new JsonValidatingParser();
		for (int i = 0; i < 10000; ++i) {
			Game parsedGame = vparser.fromJson(json, Game.class);
		}
	}
}
