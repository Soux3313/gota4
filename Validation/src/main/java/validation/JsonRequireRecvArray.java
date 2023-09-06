package validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * Requires: that the annotated ARRAY may not be null; has dimensions of size
 * `sizes` where `sizes[0]` is the size of the 0-th dimension and so on;
 * contains no null elements (only for arrays where the inner type is not
 * primitive); inner elements satisfy their validation specification
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRequireRecvArray {
	int[] sizes();
}
