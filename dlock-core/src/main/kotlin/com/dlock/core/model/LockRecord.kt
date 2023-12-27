package com.dlock.core.model

import java.time.LocalDateTime

/**
 * Represents a record of a lock.
 * The record is dedicated for saving the lock in a persistent store.
 *
 * @param lockKey The key of the lock.
 * @param lockHandleId The handle ID of the lock.
 * @param createdTime The time when the lock was created.
 * @param expirationSeconds The number of seconds after which the lock will expire.
 */
data class WriteLockRecord(val lockKey: String,
                           val lockHandleId: String,
                           val createdTime: LocalDateTime,
                           val expirationSeconds: Long)

/**
 * Represents a record of a lock.
 * The record is dedicated for reading the state of the lock from a persistent store.
 *
 * @param lockKey The key of the lock.
 * @param lockHandleId The handle ID of the lock.
 * @param createdTime The time when the lock was created.
 * @param expirationSeconds The number of seconds after which the lock will expire.
 * @param currentTime The current time for checking lock expiration.
 */
data class ReadLockRecord(val lockKey: String,
                          val lockHandleId: String,
                          val createdTime: LocalDateTime,
                          val expirationSeconds: Long,
                          val currentTime: LocalDateTime) {
    companion object {
        @JvmStatic
        fun of(lockRecord: WriteLockRecord, currentTime: LocalDateTime): ReadLockRecord {
            return ReadLockRecord(
                lockRecord.lockKey,
                lockRecord.lockHandleId,
                lockRecord.createdTime,
                lockRecord.expirationSeconds,
                currentTime
            )
        }
    }
}