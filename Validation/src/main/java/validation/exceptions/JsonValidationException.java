package validation.exceptions;

/**
 * the exception thrown if the {@link validation.JsonValidatingParser} did not successfully
 * validate an object
 */
public class JsonValidationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JsonValidationException(String msg) {
		super(msg);
	}
}
