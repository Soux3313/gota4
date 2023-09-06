package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import validation.exceptions.JsonValidationException;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.fail;

public class JsonRequireRecvRecursiveTest {

	private static JsonValidatingParser parser = new JsonValidatingParser();

	private JsonElement intoJson(Object obj) {
		Gson g = new Gson();
		return g.toJsonTree(obj);
	}


	static class Base {
		@JsonRequireRecv Integer a;
	}

	static class B {
		@JsonRequireRecvRecursive Base a;
	}

	static class BA {
		@JsonRequireRecvRecursive ArrayList<Base> a;
	}


	@Test
	public void testFailOnNull() {
		B a = new B();
		a.a = null;
		try {
			parser.fromJson(intoJson(a), B.class);
			fail("parsing succeeded on null-value");
		} catch (JsonValidationException e) {
			// expected
		}
	}

	@Test
	public void testFailOnShallow() {
		B a = new B();
		a.a = new Base();

		try {
			parser.fromJson(intoJson(a), B.class);
			fail("parsing succeeded on shallow value");
		} catch (JsonValidationException e) {
			// expected
		}
	}

	@Test
	public void testSuccessOnDeep() {
		B a = new B();
		a.a = new Base();
		a.a.a = 1;

		parser.fromJson(intoJson(a), B.class);
	}

	@Test
	public void testFailOnNullInCollection() {
		BA b = new BA();
		b.a = new ArrayList<>();
		b.a.add(null);

		try {
			parser.fromJson(intoJson(b), BA.class);
			fail("parsing suceeded on nullvalue in collection");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testFailOnShallowCorrectCollection() {
		BA b = new BA();
		b.a = new ArrayList<>();
		b.a.add(new Base());

		try {
			parser.fromJson(intoJson(b), BA.class);
			fail("parsing suceeded on shallow correctness");
		} catch (JsonValidationException _e) {
			//expected
		}
	}

	@Test
	public void testSuccessOnEmptyCollection() {
		BA b = new BA();
		b.a = new ArrayList<>();
		parser.fromJson(intoJson(b), BA.class);
	}

	@Test
	public void testSuccessOnDeepCorrectCollection() {
		Base inner = new Base();
		inner.a = 1;

		BA b = new BA();
		b.a = new ArrayList<>();
		b.a.add(inner);

		parser.fromJson(intoJson(b), BA.class);
	}

}
