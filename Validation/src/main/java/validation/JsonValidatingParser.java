package validation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import validation.exceptions.JsonValidationException;
import validation.util.ArrayUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/***
 * An extension of `Gson` allowing specific checks on expected object structure
 * view `JsonRequireRecv*` annotations for more information
 *
 * Warning: parameters may *not* be `null`, unless otherwise specified,
 * behaviour is undefined if this precondition is violated
 */
public class JsonValidatingParser {

	private Gson gson;

	/***
	 * construct a JsonValidatingParser from a default constructed `Gson` object
	 */
	public JsonValidatingParser() {
		this.gson = new Gson();
	}

	/***
	 * construct a JsonValidatingParser from a given `Gson` object
	 * 
	 * @param gson the preconstructed `Gson`
	 */
	public JsonValidatingParser(Gson gson) {
		this.gson = gson;
	}

	/***
	 * converts a JsonElement into an object of type T checking any conditions
	 * imposed by the relevant annotations on fields of the object
	 *
	 * @param json         the source json
	 * @param typeOfResult the Class for instances for T
	 * @param <T>          the target type of the conversion
	 * @return filled object of type T, satisfying all conditions imposed by the
	 *         annotations
	 * @throws JsonSyntaxException      if the json was malformed
	 * @throws JsonValidationException  if any field did not pass the checks
	 * @throws IllegalArgumentException if any annotation was erroneously placed, or
	 *                                  any precondition of any annotation did not
	 *                                  hold
	 */
	public <T> T fromJson(JsonElement json, Class<T> typeOfResult)
			throws JsonSyntaxException, JsonValidationException, IllegalArgumentException {
		T ret = this.gson.fromJson(json, typeOfResult);

		if (ret == null) {
			throw new JsonValidationException("Root object expected");
		}
		this.validateObject(ret, "$");

		return ret;
	}

	/***
	 * converts a String, which should contain json, into an object of type T
	 * checking any conditions imposed by the relevant annotations on fields of the
	 * object
	 *
	 * @param json         the source json
	 * @param typeOfResult the Class for instances for T
	 * @param <T>          the target type of the conversion
	 * @return filled object of type T, satisfying all conditions imposed by the
	 *         annotations
	 * @throws JsonSyntaxException      if the json was malformed
	 * @throws JsonValidationException  if any field did not pass the checks
	 * @throws IllegalArgumentException if any annotation was erroneously placed, or
	 *                                  any precondition of any annotation did not
	 *                                  hold
	 */
	public <T> T fromJson(String json, Class<T> typeOfResult)
			throws JsonSyntaxException, JsonValidationException, IllegalArgumentException {
		T ret = this.gson.fromJson(json, typeOfResult);

		if (ret == null) {
			throw new JsonValidationException("Root object expected");
		}
		this.validateObject(ret, "$");

		return ret;
	}

	/***
	 * Converts an object of type T into a JsonElement, this does not check any
	 * annotations, it is just a direct call to `JsonElement
	 * Gson.toJsonTree(Object)`
	 *
	 * @param obj the source object
	 * @param <T> the generic type of the source object
	 * @return valid json representing the structure and data of `obj`
	 */
	public <T> JsonElement toJsonTree(T obj) {
		return this.gson.toJsonTree(obj);
	}

	/***
	 * checks if the field f has any annotations relevant for validation
	 *
	 * @param f the field to check
	 * @return true if f has relevant annotations, false otherwise
	 */
	private boolean hasJsonReflAnnotation(Field f) {
		return f.getAnnotation(JsonRequireRecv.class) != null || f.getAnnotation(JsonRequireRecvRecursive.class) != null
				|| f.getAnnotation(JsonRequireRecvArray.class) != null
				|| f.getAnnotation(JsonRequireRecvPredicate.class) != null;
	}

	/***
	 * validates the structure and nullness of an array, this is a recursive
	 * function all parameters are accumulators, so they will change as the
	 * recursion deepens
	 *
	 * @param array              the actual object to check precondition: `array`
	 *                           must be of array type recursion behaviour: `array`
	 *                           = `array[x]` for any valid `x`
	 *
	 * @param sizes              the expected sizes of the dimensions precondition:
	 *                           num dimensions in `array` == `sizes.length`
	 *                           recursion behaviour: `sizes` = `sizes[1..]`
	 *
	 * @param needsNormalization an accumulator that is set if the array needs
	 *                           normalization; call with needsNormalization=false
	 *                           recursion behaviour: needsNormalization || (true if
	 *                           normalization nessesary else false)
	 *
	 * @param parentFieldName    the name/path of the object that contains array,
	 *                           this parameter is an accumulator recursion
	 *                           behaviour: parentFieldName = parentFieldName +
	 *                           "[x]" for any valid `x`
	 *
	 * @throws JsonValidationException  if the sizes of the dimensions do not fit or
	 *                                  the contained Objects are null
	 * @throws IllegalArgumentException if the annotation was erroneously placed on
	 *                                  a non-array field
	 */
	private boolean validateArray(final Object array, final int[] sizes, boolean needsNormalization,
			final String parentFieldName) throws JsonValidationException, IllegalArgumentException {
		if (array == null) {
			throw new JsonValidationException(String.format("null-value in field: %s", parentFieldName));
		}

		// annotation invariant violated
		if (sizes.length == 0) {
			throw new IllegalArgumentException("Erroneous `sizes` annotation on array, missing dimensions");
		}

		// check outer most dimension
		if (Array.getLength(array) < sizes[0]) {
			throw new JsonValidationException(String.format("Incorrect array size of field: %s expected %d got %d",
					parentFieldName, sizes[0], Array.getLength(array)));
		}

		// if the inner type is a primitive, we are done here since primitives `are not
		// allowed` to have annotations on them
		// this is not a java limitation but a semantic one
		if (array.getClass().getComponentType().isPrimitive()) {
			if (Array.getLength(array) > sizes[0]) {
				throw new JsonValidationException(String.format(
						"Incorrect array size of field: %s expected %d got %d; normalization impossible because of primitive element type",
						parentFieldName, sizes[0], Array.getLength(array)));
			}

			return needsNormalization;
		} else {
			Object[] asArray = (Object[]) array;

			if (Array.getLength(array) > sizes[0]) {
				boolean allNull = Arrays.stream(asArray).skip(sizes[0]).allMatch(Objects::isNull);

				if (allNull) {
					needsNormalization = true;
				} else {
					throw new JsonValidationException(
							String.format("Incorrect array size of field: %s expected %d got %d", parentFieldName,
									sizes[0], Array.getLength(array)));
				}
			}

			if (sizes.length == 1) {
				// if this is the innermost dimension of the array, do normal object checks on
				// the values

				for (int i = 0; i < sizes[0]; ++i) {
					if (asArray[i] == null) {
						throw new JsonValidationException(String.format("null-value in: %s[%d]", parentFieldName, i));
					}

					validateObject(asArray[i], String.format("%s[%d]", parentFieldName, i));
				}

			} else {
				// this is not the innermost dimension, therefore any contained `Object` is
				// actually an array
				// recurse with 1 less element in `sizes`

				int[] newSizes = Arrays.copyOfRange(sizes, 1, sizes.length);

				for (int i = 0; i < sizes[0]; ++i) {

					boolean needNorm = validateArray(asArray[i], newSizes, needsNormalization,
							String.format("%s[%d]", parentFieldName, i));
					if (needNorm) {
						needsNormalization = true;
					}
				}

			}
			return needsNormalization;
		}
	}

	/***
	 * validates if the collection is non-empty and contains only non null objects
	 *
	 * @param collection      the actual object to validate
	 * @param parentFieldName the name of the object that contains array/f
	 * @throws JsonValidationException if the collection is empty or contains
	 *                                 null-values
	 */
	private void validateCollection(Object collection, String parentFieldName) throws JsonValidationException {

		for (Object x : (Iterable<?>) collection) {
			if (x == null) {
				throw new JsonValidationException(String.format("Nullvalue in Collection: %s", parentFieldName));
			}
			this.validateObject(x, String.format("%s[]", parentFieldName));
		}
	}

	/***
	 * validates any object based on a predicate, but since annotations don't
	 * support any object as a member only the class of the validator can be passed.
	 * This has some implications: - the validator will be constructed by java
	 * reflection - therefore the validator must have exactly 1 constructor with 0
	 * parameters - therefore using this method has a severe performance impact
	 *
	 * @param classOfValidator the metainformation of the validator type
	 * @param fieldValue       the actual value of the field to be validated
	 * @param parentFieldName  the name of the parent of f (this only exists to give
	 *                         useful error messages)
	 *
	 * @throws JsonValidationException  if fieldValue == null or
	 *                                  validator.test(fieldValue) == false
	 * @throws IllegalArgumentException if the validator type does not satisfy the
	 *                                  given preconditions
	 */
	private void validatePredicate(Class<? extends Predicate<Object>> classOfValidator, Object fieldValue,
			String parentFieldName) throws JsonValidationException, IllegalArgumentException {

		try {
			// get the first constructor, which has to be one of 0 parameters
			Constructor<?> ctor = classOfValidator.getDeclaredConstructors()[0];

			// this cast is highly unsafe for some reason, i am not exactly sure why since
			// it is explicitly specified
			// that the object must implement Predicate<Object>
			@SuppressWarnings("unchecked")
			Predicate<Object> validatorInstance = (Predicate<Object>) ctor.newInstance();

			if (!validatorInstance.test(fieldValue)) {
				throw new JsonValidationException(
						String.format("did not pass validation for field: %s expected to pass test of %s",
								parentFieldName, classOfValidator.getName()));
			}

		} catch (InstantiationException | IllegalArgumentException | InvocationTargetException
				| IllegalAccessException e) {
			throw new IllegalArgumentException("could not construct validator");
		}
	}

	/***
	 * validates a fields contents based on the annotations `JsonRequireRecv`,
	 * `JsonRequireRecvRecursive` and `JsonRequireRecvArray` the behaviour of these
	 * can be looked up in the doc comments on the respective annotations
	 *
	 * @param value           the object to validate; precondition: full read access
	 *                        to any field of `object`
	 * @param parentFieldName the name of the object that contains value
	 * @throws JsonValidationException  if any condition of the annotations does not
	 *                                  hold
	 * @throws IllegalArgumentException if the annotations were erroneously placed,
	 *                                  or any other annotation preconditions do not
	 *                                  hold
	 */
	private void validateObject(Object value, String parentFieldName)
			throws JsonValidationException, IllegalArgumentException {

		for (Field f : value.getClass().getDeclaredFields()) {

			// ignore all fields that do not have relevant annotations
			if (this.hasJsonReflAnnotation(f)) {

				// necessary because classes to validate may be in different packages,
				// and reflection does not always work correctly if this flag is not set
				f.setAccessible(true);

				Object fieldValue;

				// may not be able to retrieve actual field value
				// therefore throw, since this validates a precondition
				try {
					fieldValue = f.get(value);
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw new IllegalArgumentException(
							"parser does not have full access do data, could not read from field");
				}

				final String pathOfField = String.format("%s.%s", parentFieldName, f.getName());

				// this check needs to be performed if any related annotation is found
				if (fieldValue == null) {
					throw new JsonValidationException(String.format("Missing field: %s", pathOfField));
				}

				JsonRequireRecvPredicate predicateAnnotation = f.getAnnotation(JsonRequireRecvPredicate.class);
				if (predicateAnnotation != null) {
					this.validatePredicate(predicateAnnotation.validator(), fieldValue, pathOfField);
				}

				JsonRequireRecvArray arrayAnnotation = f.getAnnotation(JsonRequireRecvArray.class);
				if (arrayAnnotation != null) {
					if (!fieldValue.getClass().isArray()) {
						throw new IllegalArgumentException(
								String.format("Erroneous array annotation on non-array field: %s", pathOfField));
					}

					int[] sizes = arrayAnnotation.sizes().length == 0 ? ArrayUtil.getNormalizedDimSizes(fieldValue)
							: arrayAnnotation.sizes();

					boolean needsNormalization = this.validateArray(fieldValue, sizes, false, pathOfField);

					if (needsNormalization) {
						try {
							f.set(value, ArrayUtil.normalizedCopyOf(fieldValue, sizes));
						} catch (IllegalAccessException e) {
							throw new IllegalArgumentException(
									"parser does not have full access do data, could not write to field");
						}
					}
				}

				if (f.getAnnotation(JsonRequireRecvRecursive.class) != null) {

					if (fieldValue instanceof Iterable) {
						this.validateCollection(fieldValue, pathOfField);
					} else {
						this.validateObject(fieldValue, pathOfField);
					}
				}
			}
		}
	}
}
