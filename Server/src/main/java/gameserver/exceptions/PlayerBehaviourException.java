package gameserver.exceptions;

import model.ids.GlobalPlayerId;

/**
 * indicates that the player behaved in a non api conformant way
 */
public class PlayerBehaviourException extends Exception {
	private final GlobalPlayerId causingPlayer;

	public PlayerBehaviourException(GlobalPlayerId p, String msg) {
		super(String.format("Player { %s } caused an exception: %s", p.get(), msg));
		this.causingPlayer = p;
	}

	public GlobalPlayerId getCausingPlayer() {
		return this.causingPlayer;
	}
}
