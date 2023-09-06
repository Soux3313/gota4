package model.ids;

import java.util.Objects;

/**
 * this class represents an id of any game on the server
 * this class is nessary to properly seperate ids from each other
 * so we don't throw integers around everywhere and the
 * compiler can check for unintentional id mix-ups
 */
public class GameId implements Comparable<GameId> {
	private int value;

	public GameId(int value) {
		this.value = value;
	}

	public int get() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GameId)) return false;
		GameId gameId = (GameId) o;
		return value == gameId.value;
	}

	@Override
	public int compareTo(GameId other) {
		return Integer.compare(this.value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public GameId clone() {
		return new GameId(this.value);
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
