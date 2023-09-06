package model.player;

import model.util.Position;

/**
 * This class represents a Move of an Amazon a {@link Player} made. This class
 * does not store information of the {@link Shot} the Amazon made.
 */
public class Move {

	/**
	 * The starting {@link Position} of the Move.
	 */
	private final Position start;

	/**
	 * The end {@link Position} of the Move.
	 */
	private final Position end;

	/**
	 * Constructor for the Move.
	 * @param start the start {@link Position}.
	 * @param end the end {@link Position}.
	 */
	public Move(Position start, Position end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Getter for the start {@link Position}.
	 * @return start {@link Position}.
	 */
	public Position getStart() {
		return start;
	}

	/**
	 * Getter for the end {@link Position}.
	 * @return the end {@link Position}.
	 */
	public Position getEnd() {
		return end;
	}

	public Move clone() {
		return new Move(start.clone(), end.clone());
	}

	@Override
	public String toString() {
		return "Move{" +
				"start=" + start +
				", end=" + end +
				'}';
	}
}
