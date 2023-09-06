package model.util;


/**
 * This class represents a Position on a {@link model.board.Board}.
 *
 * <p>Row - X</p>
 * <p>Column - Y</p>
 */
public class Position extends Vec2i implements Cloneable {

	public Position(int row, int column) {
		super(row, column);
	}

	public Position(Vec2i vec) {
		super(vec.getX(), vec.getY());
	}

	public int getRow() {
		return this.getX();
	}

	public int getColumn() {
		return this.getY();
	}


	/**
	 * Moves the position by a given {@link Vec2i}.
	 * @param p a new Position moved by the vector.
	 */
	public Position moveBy(Vec2i p) {
		return new Position(this.getX() + p.getX(), this.getY() + p.getY());
	}

	@Override
	public String toString() {
		return String.format("Position{ row: %d, column: %d }", getRow(), getColumn());
	}

	@Override
	public Position clone() {
		return new Position(super.clone());
	}
}