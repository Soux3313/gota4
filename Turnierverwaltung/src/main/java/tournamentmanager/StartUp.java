package tournamentmanager;

import model.board.Board;
import model.player.Player;
import model.util.PieceMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.stream.Stream;

public class StartUp {
	public static void main(String[] args) {

		TournamentMngrOpts opts = TournamentMngrOpts.fromArgsOrDie(args);

		Stream<Player> players;
		try (InputStream is = new FileInputStream(opts.playersFile)) {
			players = TournamentManager.parsePlayersFile(is);
		} catch (FileNotFoundException e) {
			System.err.printf("Failed to find the text file \"%s\" containing the players: %s%n", opts.playersFile, e.getMessage());
			System.exit(1);
			return;
		} catch (IOException e) {
			System.err.printf("IOException while reading players file: %s%n", e.getMessage());
			System.exit(1);
			return;
		}
		
		// starting board
		Integer[][] defaultMap = {{ -1, -1, -1,  1, -1, -1,  1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{  1, -1, -1, -1, -1, -1, -1, -1, -1,  1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{  0, -1, -1, -1, -1, -1, -1, -1, -1,  0},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
				{ -1, -1, -1,  0, -1, -1,  0, -1, -1, -1}};
		
		Board startingBoard = new Board(PieceMap.fromSquares(10, 10, defaultMap));

		System.out.println("Creating TournamentManager instance..");
		
		TournamentManager tManager = new TournamentManager(
				Duration.ofSeconds(10),
				startingBoard,
				opts.address,
				opts.token,
				opts.backupdir);

		tManager.startTournament(players);
	}
}
