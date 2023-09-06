package model.exceptions;

/**
 * an exception for when trying to construct a {@link model.ids.GamePlayerId} from an
 * int that is not 0 or 1
 */
public class InvalidGamePlayerIdException extends IllegalArgumentException {
	public InvalidGamePlayerIdException() {
		super();
	}
}
