package validation.util;

public class Pair<A, B> {
	private final A first;
	private final B second;

	public Pair(A a, B b) {
		first = a;
		second = b;
	}

	public A getFirst() {
		return this.first;
	}

	public B getSecond() {
		return this.second;
	}
}
