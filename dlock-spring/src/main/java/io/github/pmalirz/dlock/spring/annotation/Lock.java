package io.github.pmalirz.dlock.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lock annotation can be placed on a method.
 *
 * @author Przemyslaw Malirz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {
    /**
     * The key identifying the lock.
     * Must be a non-blank string, up to {@value io.github.pmalirz.dlock.api.KeyLock#MAX_LOCK_KEY_LENGTH} characters.
     * Can contain parameters referencing method arguments via {@link LockKeyParam}.
     */
    String key();

    /**
     * The lock expiration time in seconds. Must be greater than 0.
     */
    long expirationSeconds();
}
