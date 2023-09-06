package model.player;

import model.util.Position;
import validation.JsonRequireRecvRecursive;


/**
 * This class repesents a Shot an {@link model.board.pieces.Amazon} made.
 * Look at {@link Turn} class for more information.
 *
 */
public class Shot {

	/**
	 * The {@link Position} the Shot landed.
	 */
	private final Position shot;


	/**
	 * Constructor for the Shot.
	 * @param p the {@link Position} the shot should land.
	 */
	public Shot(Position p) {
		this.shot = p;
	}

	/**
	 * Getter for the {@link Position} of the Shot.
	 * @return  the Position of the Shot.
	 */
	public Position getShotPosition() {
		return shot;
	}

	public Shot clone() {
		return new Shot(shot.clone());
	}

	@Override
	public String toString() {
		return String.format("Shot{ shot: %s }", shot);
	}
}
