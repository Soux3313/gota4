package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import validation.exceptions.JsonValidationException;
import org.junit.Test;

import static org.junit.Assert.fail;

public class JsonRequireRecvTest {

	private static JsonValidatingParser parser = new JsonValidatingParser();

	private JsonElement intoJson(Object obj) {
		Gson g = new Gson();
		return g.toJsonTree(obj);
	}


	static class Base {
		@JsonRequireRecv Integer a;
	}

	static class Unannotated {
		Integer a;
		Integer b;
	}

	@Test
	public void testIgnoreNonAnnotated() {
		Unannotated a = new Unannotated();
		parser.fromJson(intoJson(a), Unannotated.class);
	}


	static class A {
		@JsonRequireRecv Base a;
	}

	@Test
	public void testJsonRequireRecvFailOnMissingRequired() {

		A a = new A();
		a.a = null;

		try {
			parser.fromJson(intoJson(a), A.class);
			fail("parsing succeeded when it shouldnt have");
		} catch (JsonValidationException e) {
			// expected
		}
	}

	@Test
	public void testJsonRequireRecvSuccessOnExistingRequiredValueShallow() {
		A a = new A();
		a.a = new Base();
		parser.fromJson(intoJson(a), A.class);
	}

	@Test
	public void testJsonRequireRecvSuccessOnExistingRequiredValueDeep() {
		A a = new A();
		a.a = new Base();
		a.a.a = 1;
		parser.fromJson(intoJson(a), A.class);
	}
}
