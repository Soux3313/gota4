package gameserver.exceptions;

import model.ids.GlobalPlayerId;

/**
 * there was an error sending a message to a player
 */
public class PlayerReceiveException extends PlayerBehaviourException {
	public PlayerReceiveException(GlobalPlayerId p, String msg) {
		super(p, String.format("player failed to receive msg: %s", msg));
	}
}
