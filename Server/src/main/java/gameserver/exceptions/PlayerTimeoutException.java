package gameserver.exceptions;

import model.ids.GlobalPlayerId;
import model.player.Player;

/**
 * This class represents an exception that is thrown if a player doesn't respond
 * to a request within a certain time. (see `maxTurnTime`)
 */
public class PlayerTimeoutException extends PlayerResponseException {

	private static final long serialVersionUID = 1L;

	public PlayerTimeoutException(GlobalPlayerId p) {
		super(p, "timeout");
	}
}