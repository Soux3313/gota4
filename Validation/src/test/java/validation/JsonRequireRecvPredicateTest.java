package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import validation.exceptions.JsonValidationException;
import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.fail;

public class JsonRequireRecvPredicateTest {

	private static JsonValidatingParser parser = new JsonValidatingParser();

	private JsonElement intoJson(Object obj) {
		Gson g = new Gson();
		return g.toJsonTree(obj);
	}

	static class GValidator implements Predicate<Object> {

		@Override
		public boolean test(Object o) {
			return o.equals("Hello World");
		}
	}

	static class G {
		@JsonRequireRecvPredicate(validator=GValidator.class) String someString;
	}


	@Test
	public void failOnNullValue() {
		G g = new G();
		g.someString = null;

		try {
			parser.fromJson(intoJson(g), G.class);
			fail("parsing succeeded even tho value was null");
		} catch (JsonValidationException e) {
			//expected
		}
	}

	@Test
	public void testFailIfPredicateFalse() {
		G g = new G();
		g.someString = "Hewwo World";

		try {
			parser.fromJson(intoJson(g), G.class);
			fail("parsing succeeded even though predicate did not hold");
		} catch (JsonValidationException e) {
			//expected
		}
	}

	@Test
	public void testIfPredicateTrue() {
		G g = new G();
		g.someString = "Hello World";
		parser.fromJson(intoJson(g), G.class);
	}
}
