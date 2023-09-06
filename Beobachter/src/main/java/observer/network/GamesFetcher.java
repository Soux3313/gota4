package observer.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonParseException;

import https.HttpClientFactory;
import model.jsonstruct.GetGamesResponse;
import model.jsonstruct.ReducedGameStruct;
import observer.exceptions.FetchGamesException;
import observer.exceptions.UnexpectedServerResponseException;
import validation.JsonValidatingParser;
import validation.exceptions.JsonValidationException;

/**
 * This class just provides a method to fetch all games from a server.
 */
public class GamesFetcher {
	
	/**
	 * Fetches a list of all games from the Server given by the {@link Connection} object
	 * @param con: Connection details of the server to fetch the Games from
	 * @return ArrayList of ReducedGameStructs
	 * @throws UnexpectedServerResponseException
	 * @throws FetchGamesException
	 */
	public static ArrayList<ReducedGameStruct> fetchGames(Connection con) throws UnexpectedServerResponseException, FetchGamesException {		
		CloseableHttpClient client;
		try {
			client = HttpClientFactory.getNewHttpsClient();
		} catch (Exception e) {
			System.out.println("Critical failure, HttpClientFactory failed to construct new http client");
			e.printStackTrace();
			
			System.exit(1);
			return null; // just to appease the java compiler
		}
		
		HttpGet get = new HttpGet(con.toURI().resolve("/games"));
		JsonValidatingParser parser = new JsonValidatingParser();
		
		try {
			
			HttpResponse response = client.execute(get);
			String body = new String(response.getEntity().getContent().readAllBytes());
			
			GetGamesResponse parsedResponse = parser.fromJson(body, GetGamesResponse.class);
			ReducedGameStruct[] parsedStructs = parsedResponse.games;

			return new ArrayList<>(Arrays.asList(parsedStructs));
			
		} catch (JsonParseException | JsonValidationException e) {
			System.err.println("The validation of the server response failed.");
			throw new UnexpectedServerResponseException(e.getMessage());
			
		} catch (IOException c) {
			c.printStackTrace();
			throw new FetchGamesException(get);
			
		} finally {
			try {
				client.close();
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
	}
}
