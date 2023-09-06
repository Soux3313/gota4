package validation.util;

import java.util.*;

public abstract class ArrayUtil {

	/***
	 * copies and normalizes an array with up to 5 dimensions
	 * where normalizing means, it cuts each dimension to the size specified in `sizes[_dimension_]`
	 *
	 * precondition: `array` is actually an array, or if not: `sizes.length` == 0
	 * precondition: `sizes.length` == number of dimensions in array
	 * precondition: for all i: `sizes[i]` less then `min length of dimension i of array`
	 *
	 * @param array the array to be normalized and copied
	 * @param sizes the desired dimension size, where `sizes[0]` is the new size of the outermost dimension
	 * @return the copied and normalized array
	 */
	public static Object normalizedCopyOf(final Object array, int... sizes) {
		if (array.getClass().isArray()) {
			Object[] tmp = Arrays.copyOf((Object[]) array, sizes[0]);
			int[] newSizes = Arrays.copyOfRange(sizes, 1, sizes.length);

			for (int i = 0; i < sizes[0]; ++i) {
				tmp[i] = normalizedCopyOf(tmp[i], newSizes);
			}

			return tmp;
		}
		return array;
	}


	/***
	 * calculates the number of dimensions in `array`; this works on the type level
	 * and therefore has no requirements for `array`
	 *
	 * @param array the array to examine
	 * @return the number of dimensions in `array`, or if `array` is not an array 0
	 */
	public static int getNumDimensions(Object array) {
		return getNumDimensionsImpl(array.getClass());
	}


	/***
	 * gives the dimension sizes of `array`, this recurses on array[0]...[0]
	 * therefore does not account for jagged arrays
	 *
	 * @param array the array to examine
	 * @return the naive dimension sizes of `array` based on recusing into the 0th element, or `new int[0]` if `array` is not an array
	 */
	public static int[] getDimSizes(final Object array) {
		if (!array.getClass().isArray()) {
			return new int[0];
		}

		int numD = getNumDimensions(array);
		int[] dimSzs = getDimSizesImpl(array, new ArrayList<>());

		// pad with zeros
		return Arrays.copyOf(dimSzs, numD);
	}


	/***
	 * calculates the length of an array without counting trailing `null` values
	 *
	 * @param array the array to examine
	 * @return the calculated length
	 */
	public static int lengthWithoutTrailingNulls(final Object[] array) {
		return (int) StreamUtil.reversed(Arrays.stream(array))
				.dropWhile(Objects::isNull)
				.count();
	}


	/***
	 * calculates the normalized dimension sizes of `array`; meaning the sizes that the array's dimensions can be
	 * (left aligned) trimmed to without loosing any actual information (non-null values)
	 *
	 * where
	 *    left-aligned-trimming means: only removing elements from the end of the array, thus reducing it's size
	 *
	 * @param array the array to be examined
	 * @return the normalized/maximally trimmed dimension sizes of `array`
	 */
	public static int[] getNormalizedDimSizes(final Object array) {
		int numD = getNumDimensions(array);
		return Arrays.copyOf(getNormalizedDimSizesImpl(array, new ArrayList<>()), numD);
	}


	/***
	 * the implementation for `getDimSizes`, read that documentation for more details
	 *
	 * @param array accumulator for the current dimension
	 * @param acc accumulator for the observed sizes
	 * @return the accumulated sizes, missing padding zeros for 0-sized dimensions
	 */
	private static int[] getDimSizesImpl(final Object array, ArrayList<Integer> acc) {
		if (array.getClass().isArray()) {
			Object[] arr = (Object[]) array;

			if (arr.length > 0) {
				acc.add(arr.length);
				return getDimSizesImpl(arr[0], acc);
			} else {
				acc.add(0);
			}
		}

		return acc.stream()
				.mapToInt(Integer::intValue)
				.toArray();
	}


	/***
	 * the implementation for `getNumDimensions`, read that doc for more info
	 *
	 * @param arrayType the type of the array to examine
	 * @return the number of dimensions an instance of `arrayType` has
	 */
	private static int getNumDimensionsImpl(Class<?> arrayType) {
		if (arrayType.isArray()) {
			return 1 + getNumDimensionsImpl(arrayType.getComponentType());
		}
		return 0;
	}


	/***
	 * the implementation for `getNormalizedDimSizes`, read that doc for more info
	 *
	 * @param array accumulator for the currently examined dimension
	 * @param acc accumulator for the observed dimension sizes
	 * @return the normalized dimension sizes, missing padding zeros for 0-sized dimensions
	 */
	private static int[] getNormalizedDimSizesImpl(final Object array, ArrayList<Integer> acc) {
		if (array.getClass().isArray()) {
			if (array.getClass().getComponentType().isArray()) {
				Object[][] arr = (Object[][]) array;

				Optional<Object[]> nextDown = Arrays.stream(arr)
						.filter(Objects::nonNull)
						.map(a -> new Pair<>(a, lengthWithoutTrailingNulls(a)))
						.max(Comparator.comparingLong(Pair::getSecond))
						.map(Pair::getFirst);

				if (nextDown.isEmpty()) {
					acc.add(0);
				} else {
					acc.add(lengthWithoutTrailingNulls(arr));
					return getNormalizedDimSizesImpl(nextDown.get(), acc);
				}
			} else {
				Object[] arr = (Object[]) array;
				acc.add(lengthWithoutTrailingNulls(arr));
			}
		}

		return acc.stream().mapToInt(Integer::intValue).toArray();
	}
}
