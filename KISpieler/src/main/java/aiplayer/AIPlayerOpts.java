package aiplayer;

import com.google.gson.JsonParseException;
import org.apache.commons.cli.*;
import validation.JsonRequireRecv;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AIPlayerOpts {
	@JsonRequireRecv
	public String hostname;

	@JsonRequireRecv
	public Integer port;

	@JsonRequireRecv
	public Boolean insecure;

	public AIPlayerOpts(String hostname, int port, boolean insecure) {
		this.hostname = hostname;
		this.port = port;
		this.insecure = insecure;
	}

	public static AIPlayerOpts fromArgsOrDie(String[] args) {
		try {
			return AIPlayerOpts.fromArgs(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);

			// java compiler cannot figure out that this is unreachable
			// i can't tell it that it's unreachable
			// i can't return null because then on every use the ide complains that the return value may be null
			// so i just have to put some garbage code here
			return (AIPlayerOpts) new Object();
		}
	}

	public static AIPlayerOpts fromArgs(String[] args) throws IllegalArgumentException {
		Options options = new Options();

		Option hostname = new Option("hostname",true,
				"The hostname of the machine which runs the ai player. Can be set to 0.0.0.0 for simplicity.");
		hostname.setRequired(false);
		hostname.setArgName("ServerHostname");
		options.addOption(hostname);

		Option port = new Option("port",true,
				"The port on which the ai player listens");
		port.setRequired(false);
		port.setArgName("ServerPort");
		options.addOption(port);

		Option insecure = new Option("insecure", false,
				"Disables TLS (disables HTTPS and uses HTTP instead)");
		insecure.setRequired(false);
		options.addOption(insecure);

		//Begin parsing:
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		String hostnameValue = "localhost"; // default hostname
		int portValue = 33098; // default port

		try {
			cmd = parser.parse(options, args);

			if(cmd.hasOption("hostname")) {
				hostnameValue = cmd.getOptionValue("hostname");
			}

			if(cmd.hasOption("port")) {
				portValue = Integer.parseInt(cmd.getOptionValue("port"));
			}
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

		return new AIPlayerOpts(hostnameValue, portValue, cmd.hasOption("insecure"));
	}

	public static AIPlayerOpts fromJson(String json) throws JsonParseException, JsonValidationException {
		JsonValidatingParser parser = new JsonValidatingParser();
		return parser.fromJson(json, AIPlayerOpts.class);
	}

}
