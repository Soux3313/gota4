package tournamentmanager;

import org.apache.commons.cli.*;
import validation.JsonRequireRecv;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;

public class TournamentMngrOpts {
	@JsonRequireRecv
	public final String playersFile;

	@JsonRequireRecv
	public final String address;

	@JsonRequireRecv
	public final String token;

	@JsonRequireRecv
	public final String backupdir;

	public TournamentMngrOpts(String playersFile, String address, String token, String backupdir) {
		this.playersFile = playersFile;
		this.address = address;
		this.token = token;
		this.backupdir = backupdir;
	}

	public static TournamentMngrOpts fromArgsOrDie(String[] args) {
		try {
			return fromArgs(args);
		} catch (Exception e) {
			System.err.println("could not parse commandline: " + e.getMessage());
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (TournamentMngrOpts) new Object();
		}
	}

	public static TournamentMngrOpts fromArgs(String[] args) throws IllegalArgumentException, IOException {
		Options options = new Options();

		Option players = new Option("players",true, "text file containing the players");
		players.setRequired(false);
		players.setArgName("Players");
		options.addOption(players);

		Option address = new Option("address",true, "server address, defaults to https://localhost:33100");
		address.setRequired(false);
		address.setArgName("ServerAddress");
		options.addOption(address);

		Option token = new Option("token",true, "server token");
		token.setRequired(false);
		token.setArgName("Token");
		options.addOption(token);

		Option backup = new Option("backupdir",true, "directory containing the backup files");
		backup.setRequired(false);
		backup.setArgName("BackupDirectory");
		options.addOption(backup);

		//Begin parsing:
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

		// default player file, "user.dir" refers to the directory the jar is located in
		String playersFileSrc = Path.of(System.getProperty("user.dir"), "spieler.txt").toString();

		// default server address
		String serverAddress = "https://localhost:33100";

		// default backup dir, "user.dir" refers to the directory the jar is located in
		String backupDirectorySrc = System.getProperty("user.dir");

		// default server token
		String serverToken = "31415926535897932384626433832795";

		try {
			cmd = parser.parse(options, args);

			if(cmd.hasOption("players")) {
				playersFileSrc = cmd.getOptionValue("players");
			}

			if(cmd.hasOption("address")) {
				serverAddress = cmd.getOptionValue("address");
			}

			if(cmd.hasOption("token")) {
				serverToken = cmd.getOptionValue("token");
			}

			if(cmd.hasOption("backupdir")) {
				backupDirectorySrc = cmd.getOptionValue("backupdir");
			}

			return new TournamentMngrOpts(playersFileSrc, serverAddress, serverToken, backupDirectorySrc);
		} catch (NumberFormatException | ParseException e) {
			//Invalid argument:

			HelpFormatter formatter = new HelpFormatter();
			StringWriter out = new StringWriter();
			PrintWriter pw = new PrintWriter(out);

			formatter.printHelp(pw,
					80,
					"utility-name",
					"",
					options,
					formatter.getLeftPadding(),
					formatter.getDescPadding(),
					"");

			pw.flush();

			String msg = String.format("invalid program args:\n%s\n%s\n",
					e.getMessage(),
					out.toString());

			throw new IllegalArgumentException(msg);
		}
	}

}
