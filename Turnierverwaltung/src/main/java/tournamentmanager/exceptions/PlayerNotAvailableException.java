package tournamentmanager.exceptions;

import model.player.Player;

public class PlayerNotAvailableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Player causingPlayer;

	public PlayerNotAvailableException(Player p) {
		this.causingPlayer = p;
	}

	public Player getCausingPlayer() {
		return this.causingPlayer;
	}
}
