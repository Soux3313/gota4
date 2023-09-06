package model.exceptions;

import model.ids.GameId;

/**
 * This class represents an exception that is thrown if adding a new game to the
 * Gameserver fails due to the gameId being already in use.
 */
public class GameIdAlreadyInUseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The game object that was tried to be added and got rejected.
	 */
	private final GameId referencedGame;

	/**
	 * The constructor of the IdAlreadyInUseException class.
	 * 
	 * @param message the message String with some information of why this exception
	 *                was thrown.
	 * @param g       the game object that got rejected by the Gameserver and thus
	 *                could not be added.
	 */
	public GameIdAlreadyInUseException(String message, GameId g) {
		super(message);
		this.referencedGame = g;
	}

	/**
	 * Getter for the referencedGame attribute.
	 * 
	 * @return the referencedGame object of this exception.
	 */
	public GameId getReferencedGame() {
		return referencedGame;
	}

}
