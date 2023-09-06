package model.jsonstruct;

import model.player.Player;
import model.ids.GlobalPlayerId;
import validation.JsonRequireRecv;

/**
 * This defines the structure of a player in Json format as specified by the api
 */
public class PlayerStruct {
	@JsonRequireRecv
	public Integer playerId;

	@JsonRequireRecv
	public String name;

	@JsonRequireRecv
	public String url;

	/**
	 * Turns the PlayerStruct attributes into a Player
	 *
	 * @return a player with the corresponding values
	 */
	public Player intoModel() {
		return new Player(new GlobalPlayerId(playerId), name, url);
	}

	public static PlayerStruct fromModel(Player p, boolean authenticated) {
		PlayerStruct self = new PlayerStruct();
		self.playerId = p.getPlayerId().get();
		self.name = p.getName();

		if (authenticated) {
			self.url = p.getUrl();
		}

		return self;
	}
}