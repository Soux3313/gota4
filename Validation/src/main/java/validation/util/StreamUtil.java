package validation.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtil {

	/***
	 * consumes a stream to give that same stream but in reverse order
	 *
	 * @param str the stream to be reversed
	 * @param <T> the componentType of the stream
	 * @return the reversed stream
	 */
	@SuppressWarnings("unchecked")
	public static <T> Stream<T> reversed(Stream<T> str) {
		Object[] tmp = str.toArray();
		return (Stream<T>) IntStream.range(0, tmp.length).mapToObj(i -> tmp[tmp.length - i - 1]);
	}

	/***
	 * enumerates the objects in a stream meaning, pairing them with their respective index
	 * example:
	 * 	{ x,y,z } -> { (0,x), (1,y), (2,z) }
	 *
	 * @param stream the stream to be enumerated
	 * @param <T> the component type of the stream
	 * @return the enumerated stream
	 */
	public static <T> Stream<Pair<Integer, T>> enumerated(Stream<T> stream) {
		AtomicInteger i = new AtomicInteger();
		return stream.map(x -> new Pair<>(i.getAndIncrement(), x));
	}
}
