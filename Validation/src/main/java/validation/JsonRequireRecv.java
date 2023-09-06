package validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * Requires: - that the annotated object may not be null
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRequireRecv {
}