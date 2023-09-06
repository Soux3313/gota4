package gameserver.exceptions;

import model.ids.GlobalPlayerId;

/**
 * the player responded but the body of the request was malformed
 * (so invalid json or missing information)
 */
public class MalformedPlayerResponseException extends PlayerResponseException {
	public MalformedPlayerResponseException(GlobalPlayerId p, String msg) {
		super(p, String.format("malformed response: %s", msg));
	}
}
