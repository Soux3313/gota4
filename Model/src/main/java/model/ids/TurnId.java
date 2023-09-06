package model.ids;

import java.util.Objects;

/**
 * this class represents an id of any turn of a game
 * this class is nessary to properly seperate ids from each other
 * so we don't throw integers around everywhere and the
 * compiler can check for unintentional id mix-ups
 */
public class TurnId implements Cloneable, Comparable<TurnId> {
	private int value;

	public TurnId(int value) {
		this.value = value;
	}

	public int get() {
		return this.value;
	}

	@Override
	public int compareTo(TurnId other) {
		return Integer.compare(this.value, other.value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TurnId)) return false;
		TurnId turnId = (TurnId) o;
		return value == turnId.value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public TurnId clone() {
		return new TurnId(this.value);
	}
}
