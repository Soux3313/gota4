package model.exceptions;

/**
 * This is thrown when a turn cant be applied to the board because the
 * turn is invalid
 */
public class InvalidTurnException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidTurnException(String msg) {
		super(msg);
	}
}
