package io.github.pmalirz.dlock.core.model;

import java.time.LocalDateTime;

/**
 * Represents a record of a lock.
 * The record is dedicated for saving the lock in a persistent store.
 *
 * @param lockKey           The key of the lock.
 * @param lockHandleId      The handle ID of the lock.
 * @param createdTime       The time when the lock was created.
 * @param expirationSeconds The number of seconds after which the lock will
 *                          expire.
 */
public record WriteLockRecord(String lockKey,
        String lockHandleId,
        LocalDateTime createdTime,
        long expirationSeconds) {
}
