package gameserver;

import com.google.gson.JsonParseException;
import org.apache.commons.cli.*;
import validation.JsonRequireRecv;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class GameserverOpts {

	@JsonRequireRecv
	public final String hostname;

	@JsonRequireRecv
	public final Integer port;

	@JsonRequireRecv
	public final String token;

	@JsonRequireRecv
	public final Boolean insecure;

	@JsonRequireRecv
	public final Boolean debugMode;

	@JsonRequireRecv
	public final Boolean logTraffic;

	/**
	 * ctor
	 *
	 * @param hostname the address to listen on
	 * @param port the port to listen on
	 * @param token the token needed to retreive data from this server
	 * @param insecure whether or not tls should be used (where insecure=true => no tls)
	 * @param debugMode whether or not the server should print DEBUG level log messages
	 * @param logTraffic specifies whether the gameserver should log traffic level messages
	 */
	public GameserverOpts(String hostname, int port, String token, boolean insecure, boolean debugMode, boolean logTraffic) {
		this.hostname = hostname;
		this.port = port;
		this.token = token;
		this.insecure = insecure;
		this.debugMode = debugMode;
		this.logTraffic = logTraffic;
	}

	/**
	 * tries to construct GameserverOpts from command line arguments
	 * but exits the program if it failes to do so
	 *
	 * @param args the commandline arguments
	 * @return a definitely valid instance of {@link GameserverOpts} since the program terminates otherwise
	 */
	public static GameserverOpts fromArgsOrDie(String[] args) {
		try {
			return GameserverOpts.fromArgs(args);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (GameserverOpts) new Object();
		}
	}

	/**
	 * tries to construct {@link GameserverOpts} from the given commandline arguments
	 *
	 * @param args the commandline arguments
	 * @return valid GameserverOpts based on the arguments
	 * @throws IllegalArgumentException if the commandline arguments could not be parsed
	 */
	public static GameserverOpts fromArgs(String[] args) throws IllegalArgumentException {

		Options options = new Options();

		Option address = new Option("hostname",true, "the IP-address/URL of the host running the server");
		address.setRequired(false);
		address.setArgName("ServerHostname");
		options.addOption(address);

		Option port = new Option("port",true, "the port on which to run the server");
		port.setRequired(false);
		port.setArgName("ServerPort");
		options.addOption(port);

		Option token = new Option("token",true, "the token/password that has to be provided by clients to gain full access to the REST API");
		token.setRequired(false);
		token.setArgName("Token");
		options.addOption(token);

		Option debug = new Option("debug",false,
				"activates the debug mode, which causes the server to log additional debug messages");
		debug.setRequired(false);
		options.addOption(debug);

		Option traffic = new Option("traffic", false,
				"activates the traffic logging mode, where the server will log all the traffic passing through it");
		traffic.setRequired(false);
		options.addOption(traffic);

		Option insecure = new Option("insecure", false,
				"disables tls");
		insecure.setRequired(false);
		options.addOption(insecure);

		//Begin parsing:
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);

			String addressInput = "localhost";
			if(cmd.hasOption("hostname")) {
				addressInput = cmd.getOptionValue("hostname");
			}
			
			int portInput = 33100;
			if(cmd.hasOption("port")) {
				portInput = Integer.parseInt(cmd.getOptionValue("port"));
			}
			
			String tokenInput = "31415926535897932384626433832795";
			if(cmd.hasOption("token")) {
				tokenInput = cmd.getOptionValue("token");
			}
			
			boolean debugInput = cmd.hasOption("debug");
			boolean trafficInput = cmd.hasOption("traffic");
			boolean insecureInput = cmd.hasOption("insecure");

			return new GameserverOpts(addressInput, portInput, tokenInput, insecureInput, debugInput, trafficInput);

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

			String msg = String.format("invalid program args:%n%s%n%s",
					e.getMessage(),
					out.toString());

			throw new IllegalArgumentException(msg);
		}
	}

	public static GameserverOpts fromJson(String json) throws JsonParseException, JsonValidationException {
		JsonValidatingParser parser = new JsonValidatingParser();
		return parser.fromJson(json, GameserverOpts.class);
	}

	public static GameserverOpts fromJsonStream(InputStream stream) throws IOException, JsonParseException, JsonValidationException {
		byte[] content = stream.readAllBytes();
		return GameserverOpts.fromJson(new String(content));
	}
}
