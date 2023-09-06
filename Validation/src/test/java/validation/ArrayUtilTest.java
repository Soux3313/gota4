package validation;

import validation.util.ArrayUtil;
import org.junit.Test;
import java.util.Arrays;

import static org.junit.Assert.*;


public class ArrayUtilTest {

	@Test
	public void testGetDimSizes() {
		Integer[][] arr = new Integer[][] { new Integer[]{1,2,3}, new Integer[]{1,2} };
		int[] sizes = ArrayUtil.getDimSizes(arr);

		if (!Arrays.equals(sizes, new int[]{2, 3})) {
			fail("asd");
		}
	}

	@Test
	public void testGetNumDimensions() {
		Integer[][][][] arr = new Integer[][][][]{};
		int numD = ArrayUtil.getNumDimensions(arr);
		assertEquals(4, numD);
	}

	@Test
	public void testGetLengthWithoutTrailingNulls() {
		Integer[] arr = new Integer[]{ 1,2,null,4,5,null,null,8,null,null,null};
		Integer[] arr2 = new Integer[]{1,2,3};
		assertEquals(8, ArrayUtil.lengthWithoutTrailingNulls(arr));
		assertEquals(3, ArrayUtil.lengthWithoutTrailingNulls(arr2));
	}

	@Test
	public void testGetNormalizedDimSizes() {
		Integer[][] arr = new Integer[][] {
				new Integer[]{1,2,3,null,null,null},
				new Integer[]{1,2,null,null},
				new Integer[]{1,2},
				null,
				null,
				new Integer[]{1,2,3,4,null,null}
		};

		int[] dimSzs = ArrayUtil.getNormalizedDimSizes(arr);

		assertArrayEquals(new int[]{ 6, 4 }, dimSzs);
	}
}
