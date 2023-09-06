package model.ids;

import model.exceptions.InvalidGamePlayerIdException;

/**
 * this class represents a game local player id
 * so the kind that is either 0 or 1
 * this class is nessesary (in combination with {@link GlobalPlayerId}) to more clearly
 * seperate the cases where each one is used
 *
 * in constrast to {@link GlobalPlayerId}: this is basically just 0 or 1 (or more specifically PLAYER1 or PLAYER2)
 */
public enum GamePlayerId {
	PLAYER1,
	PLAYER2;

	/**
	 * constructs a GamePlayerId from an in that is either 0 or 1
	 *
	 * @param i the integer representation of the desired id
	 * @return a valid {@link GamePlayerId}
	 * @throws InvalidGamePlayerIdException if `i` is not 0 or 1
	 */
	public static GamePlayerId fromInt(int i) throws InvalidGamePlayerIdException {
		switch (i) {
			case 0: return GamePlayerId.PLAYER1;
			case 1: return GamePlayerId.PLAYER2;
			default: throw new InvalidGamePlayerIdException();
		}
	}

	/**
	 * @return the integer representation of this id
	 */
	public int get() {
		switch (this) {
			case PLAYER1: return 0;
			case PLAYER2: return 1;
			default: throw new IllegalStateException("unreachable");
		}
	}

	/**
	 * conviniece method to get the other {@link GamePlayerId} since there are only 2
	 * (one for each player)
	 *
	 * @return the local id of the other player
	 */
	public GamePlayerId other() {
		switch (this) {
			case PLAYER1: return PLAYER2;
			case PLAYER2: return PLAYER1;
			default: throw new IllegalStateException("unreachable");
		}
	}
}
