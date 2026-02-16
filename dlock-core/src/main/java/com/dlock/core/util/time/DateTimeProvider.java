package com.dlock.core.util.time;

import java.time.LocalDateTime;

/**
 * Managed date/time provider. With this testability is increased.
 *
 * @author Przemyslaw Malirz
 */
public interface DateTimeProvider {

    /**
     * Default singleton implementation.
     */
    DateTimeProvider SYSTEM = new DateTimeProvider() {
    };

    /**
     * Returns LocalDateTime.now(). It's not static so can be mocked / replaced by
     * any NOW provider.
     * The hardest part with NOW in unit tests is it's always different ;)
     */
    default LocalDateTime now() {
        return LocalDateTime.now();
    }
}
