package gameserver.exceptions;

import model.ids.GlobalPlayerId;

/**
 * the player send an invalid response
 */
public class PlayerResponseException extends PlayerBehaviourException {

	public PlayerResponseException(GlobalPlayerId p, String msg) {
		super(p, String.format("invalid response: %s", msg));
	}
}
