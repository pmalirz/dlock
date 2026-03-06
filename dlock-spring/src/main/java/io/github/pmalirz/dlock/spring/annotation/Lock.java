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
     * The lock key. Must be non-blank and up to 1000 characters.
     */
    String key();

    /**
     * The lock expiration time in seconds. Must be greater than 0.
     */
    long expirationSeconds();
}
