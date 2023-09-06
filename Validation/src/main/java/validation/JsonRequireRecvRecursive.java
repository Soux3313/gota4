package validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * Requires: - that the annotated object may not be null - and any objects contained
 * in the annotated object pass validation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRequireRecvRecursive {
}
