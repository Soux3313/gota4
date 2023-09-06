package model.jsonstruct;

import model.game.Game;
import model.game.GameState;
import validation.JsonRequireRecv;
import validation.JsonRequireRecvArray;

import java.util.Arrays;


/***
 * this is the structure for games that will be in the response for 'GET /games/?token=token' as specified by the api
 */
public class ReducedGameStruct {

	@JsonRequireRecv
	public Long maxTurnTime;

	@JsonRequireRecv
	public Integer gameId;

	@JsonRequireRecvArray(sizes={2})
	public PlayerStruct[] players;

	public Integer winningPlayer;

	@Override
	public String toString() {
		return players[0].name + " vs. " + players[1].name;
	}

	public static ReducedGameStruct fromModel(Game g, boolean authenticated) {
		ReducedGameStruct self = new ReducedGameStruct();
		self.maxTurnTime = g.getMaxTurnTime().toMillis();
		self.gameId = g.getGameId().get();
		self.players = Arrays.stream(g.getPlayers())
				.map(p -> PlayerStruct.fromModel(p, authenticated))
				.toArray(PlayerStruct[]::new);

		if (g.getGameState() == GameState.FINISHED) {
			self.winningPlayer = g.getWinningPlayer().get().get();
		}

		return self;
	}
}
