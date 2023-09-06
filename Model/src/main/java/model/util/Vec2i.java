package model.util;

import java.util.Objects;

/**
 * this class represents a 2d (mathematical) vector
 */
public class Vec2i implements Cloneable {
	private int x;
	private int y;

	public Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static Vec2i zeroed() {
		return new Vec2i(0, 0);
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Vec2i add(Vec2i other) {
		return new Vec2i(this.x + other.x, this.y + other.y);
	}

	public Vec2i sub(Vec2i other) {
		return new Vec2i(this.x - other.x, this.y - other.y);
	}

	public Vec2i mul(int lambda) {
		return new Vec2i(this.x * lambda, this.y * lambda);
	}

	@Override
	public Vec2i clone() {
		return new Vec2i(this.x, this.y);
	}

	/**
	 * This method checks if two given vectors `this` and `other` share the same row
	 * or column and thus the vector through a and b is orthogonal to the x or y
	 * axis of the 2D plane
	 *
	 * @param other the second position object
	 * @return true if the vector through a and b is orthogonal to the x or y axis
	 */
	public boolean isOrthogonalTo(Vec2i other) {
		return this.getY() == other.getY() || this.getX() == other.getX();
	}


	/**
	 * This method checks if two given vectors objects `this` and `other` are diagonal to each other
	 *
	 * @param other the second position object
	 * @return true if a and b are orthogonal
	 */
	public boolean isDiagonalTo(Vec2i other) {
		return Math.abs(this.getX() - other.getX()) == Math.abs(this.getY() - other.getY());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec2i vec2i = (Vec2i) o;
		return x == vec2i.x && y == vec2i.y;
	}

	public boolean isInDisc(Vec2i other, int epsilon) {
		return (Math.abs(this.getX() - other.getX()) <= epsilon) && (Math.abs(this.getY()- other.getY()) <= epsilon);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("Vec2i{ x: %d, y: %d }", x, y);
	}
}
