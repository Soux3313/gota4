package aiplayer;

import aiplayer.controller.TurnRequestHandler;
import com.sun.net.httpserver.HttpServer;
import https.HttpServerFactory;

import java.net.InetSocketAddress;

public class StartUp {

	public static void main(String[] args) {

		AIPlayerOpts opts = AIPlayerOpts.fromArgsOrDie(args);
		//AIPlayerOpts opts = new AIPlayerOpts("localhost", 8001, false);

		/*AIPlayer ai = new AIPlayerSwitch(
				new float[] {125, 64, 27, 8, 1}, 0.9f,
				new float[] {32, 16, 8, 4, 2}, 0.8f,
				5, 1, 10000);*/
		AIPlayer aiAlphaBeta = new AIPlayerAlphaBeta(5, new int[]{800, 350, 160, 80}, new float[] {125, 64, 27, 8, 1}, 0.9f, 1);
		//AIPlayer ai = new AIPlayerGreedy(5, new float[] {125, 64, 27, 8, 1}, 0.9f, 1, 60000);
		AIPlayer aiPlayer = new AIBuildIn(5, new float[] {125, 64, 27, 8, 1}, 0.9f, 1, aiAlphaBeta);

		HttpServer server;
		if (opts.insecure) {
			server = HttpServerFactory.makeHttpServerOrDie(new InetSocketAddress(opts.hostname, opts.port));
		} else {
			server = HttpServerFactory.makeHttpsServerOrDie(new InetSocketAddress(opts.hostname, opts.port));
		}

		server.createContext("/", new TurnRequestHandler(aiPlayer));
		server.start();
		System.out.printf("Listening on %s://%s:%d%n", opts.insecure ? "http" : "https", opts.hostname, opts.port);
	}
}
