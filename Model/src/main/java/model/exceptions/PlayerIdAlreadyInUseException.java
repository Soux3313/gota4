package model.exceptions;

import model.player.Player;

/**
 * This class represents an exception that is thrown if adding a new player to
 * the Gameserver fails due to the playerId being already in use.
 */
public class PlayerIdAlreadyInUseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The player object that was tried to be added and got rejected.
	 */
	private Player referencedPlayer;

	/**
	 * The constructor of the IdAlreadyInUseException class.
	 * 
	 * @param message the message String with some information of why this exception
	 *                was thrown.
	 * @param p       the player object that got rejected by the Gameserver and thus
	 *                could not be added.
	 */
	public PlayerIdAlreadyInUseException(String message, Player p) {
		super(message);
		this.referencedPlayer = p;
	}

	/**
	 * Getter for the referencedPlayer attribute.
	 * 
	 * @return the referencedPlayer object of this exception.
	 */
	public Player getReferencedPlayer() {
		return referencedPlayer;
	}

}
