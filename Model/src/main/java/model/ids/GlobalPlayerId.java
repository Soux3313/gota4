package model.ids;

import java.util.Objects;

/**
 * this class represents a global player id
 * so the kind that is POSTed to the /players route
 * this class is nessesary (in combination with {@link GamePlayerId}) to more clearly
 * seperate the cases where each one is used
 *
 * in constrast to {@link GamePlayerId}: this can be any integer and not just 0 or 1
 */
public class GlobalPlayerId implements Comparable<GlobalPlayerId> {
	private final int value;

	public GlobalPlayerId(int value) {
		this.value = value;
	}

	public int get() {
		return value;
	}

	@Override
	public int compareTo(GlobalPlayerId other) {
		return Integer.compare(this.value, other.value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GlobalPlayerId)) return false;
		GlobalPlayerId that = (GlobalPlayerId) o;
		return value == that.value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public GlobalPlayerId clone() {
		return new GlobalPlayerId(this.value);
	}
}
