package com.dlock.util.time

import java.time.LocalDateTime

/**
 * Managed date/time provider. With this testability is increased.
 *
 * @author Przemyslaw Malirz
 */
interface DateTimeProvider {

    companion object DefaultDateTimeProvider : DateTimeProvider

    /**
     * Returns LocalDateTime.now(). It's not static so can be mocked / replaced by any NOW provider.
     * The hardest part with NOW in unit tests is it's always different ;)
     */
    fun now(): LocalDateTime = LocalDateTime.now()

}