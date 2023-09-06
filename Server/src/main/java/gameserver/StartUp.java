package gameserver;

public class StartUp {

	public static void main(String[] args) {

		GameserverOpts opts = GameserverOpts.fromArgsOrDie(args);
		//GameserverOpts opts = new GameserverOpts("localhost", 8000, "hackeraccesstoken", false, true, true);

		Gameserver server = new Gameserver(opts);
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

		server.start();
	}
}
