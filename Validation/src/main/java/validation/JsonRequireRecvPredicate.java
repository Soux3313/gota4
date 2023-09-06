package validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Predicate;

/**
 * requires that the annotated object may not be null
 * and the validator returns `true`
 *
 * this should probably not be used since it is inefficient and kind of ugly
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRequireRecvPredicate {
	Class<? extends Predicate<Object>> validator();
}
