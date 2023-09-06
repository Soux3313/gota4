package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import validation.exceptions.JsonValidationException;
import org.junit.Test;

import static org.junit.Assert.fail;

public class JsonRequireRecvArrayTest {

	private static JsonValidatingParser parser = new JsonValidatingParser();

	private JsonElement intoJson(Object obj) {
		Gson g = new Gson();
		return g.toJsonTree(obj);
	}


	static class Base {
		@JsonRequireRecv Integer a;
	}

	static class C {
		@JsonRequireRecvArray(sizes={}) int[] a;
	}

	static class D {
		@JsonRequireRecvArray(sizes={2}) int[] a;
	}

	static class E {
		@JsonRequireRecvArray(sizes={2, 2}) int[][] a;
	}

	static class F {
		@JsonRequireRecvArray(sizes={2}) Base[] a;
	}

	@Test
	public void testFailOnNullValue() {
		C c = new C();
		try {
			parser.fromJson(intoJson(c), C.class);
			fail("parsing succeeded on null value");
		} catch (JsonValidationException _e) {
			// expected
		}
	}


	@Test
	public void testFailOnSizeTooLow() {
		D d = new D();
		d.a = new int[]{ 1 };
		try {
			parser.fromJson(intoJson(d), D.class);
			fail("parsing suceeded on invalid length (too short)");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testFailOnSizeTooLarge() {
		D d = new D();
		d.a = new int[]{ 1,2,3 };
		try {
			parser.fromJson(intoJson(d), D.class);
			fail("parsing suceeded on invalid length (too long)");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testSuccessOnValidLength() {
		D d = new D();
		d.a = new int[]{1,2};
		parser.fromJson(intoJson(d), D.class);
	}

	@Test
	public void testMultiDFailOnInvalidInnerLength() {
		E e = new E();
		e.a = new int[][] { new int[]{ 1,2 }, new int[]{ 1 } };
		try {
			parser.fromJson(intoJson(e), E.class);
			fail("parsing suceeded on invalid inner length (too short)");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testMultiDFailOnInnerNullvalue() {
		E e = new E();
		e.a = new int[][] { new int[]{ 1,2 }, null };
		try {
			parser.fromJson(intoJson(e), E.class);
			fail("parsing succeeded on inner nullvalue");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testMultiDSuccessOnValid() {
		E e = new E();
		e.a = new int[][]{ new int[]{ 1,2 }, new int[]{ 3,4 } };
		parser.fromJson(intoJson(e), E.class);
	}

	@Test
	public void testObjectArrayFailOnInnerNullvalue() {
		F f = new F();
		f.a = new Base[] { null, new Base() };
		try {
			parser.fromJson(intoJson(f), F.class);
			fail("parsing suceeded on inner nullvalue in object array");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testObjectArrayFailOnInnerValueInvalid() {
		Base inner = new Base();
		inner.a = 0;

		F f = new F();
		f.a = new Base[]{ new Base(), inner };

		try {
			parser.fromJson(intoJson(f), F.class);
			fail("parsing suceeded on invalid inner value in object array");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testObjectArraySuccessOnValidValues() {
		Base inner = new Base();
		inner.a = 0;

		F f = new F();
		f.a = new Base[] { inner, inner };
		parser.fromJson(intoJson(f), F.class);
	}


	static class H {
		@JsonRequireRecvArray(sizes={2}) Integer[] a;
	}

	@Test
	public void testSucceedOnTrailingNullValues() {
		String json = "{ \"a\": [1,2,,,,] }";
		H h = parser.fromJson(json, H.class);
	}

	@Test
	public void testStripTrailingNullValues() {
		String json = "{ \"a\": [1,2,,,,] }";
		H h = parser.fromJson(json, H.class);

		if (h.a.length != 2) {
			fail("parsed did not strip trailing commas/null values");
		}
	}

	@Test
	public void testFailOnTrailingNonNullValues() {
		String json = "{ \"a\": [1,2,,3,,] }";
		try {
			H h = parser.fromJson(json, H.class);
			fail("parser ignored trailing non null values");
		} catch (JsonValidationException e) {
			// success
		}
	}

	static class I {
		@JsonRequireRecvArray(sizes={2,2}) Integer[][] a;
	}

	@Test
	public void testMultiDimSucceedOnTrailingNullValues() {

		String json = "{ \"a\": [[1,2,], [1,2,,],,,] }";
		I i = parser.fromJson(json, I.class);
	}

	@Test
	public void testMultiDimStripTrailingNullValuesOuter() {
		String json = "{ \"a\": [[1,2], [1,2],,,,] }";
		I i = parser.fromJson(json, I.class);

		if (i.a.length != 2) {
			fail("outer dimension wrong length");
		}
	}

	@Test
	public void testMultiDimStripTrailingNullValuesInner() {
		String json = "{ \"a\": [[1,2,], [1,2,,]] }";
		I i = parser.fromJson(json, I.class);

		if (i.a[0].length != 2) {
			fail("inner dimension wrong length, fail on first inner");
		}

		if (i.a[1].length != 2) {
			fail("inner dimension wrong length, fail on second inner");
		}
	}
}
