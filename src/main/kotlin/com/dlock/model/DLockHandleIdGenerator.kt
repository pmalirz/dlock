package com.dlock.model

/**
 * Generate unique handle ID when a lock is created.
 *
 * @author Przemyslaw Malirz
 */
interface DLockHandleIdGenerator {
    fun generate(): String
}