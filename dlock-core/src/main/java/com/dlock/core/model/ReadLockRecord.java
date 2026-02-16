package com.dlock.core.model;

import java.time.LocalDateTime;

/**
 * Represents a record of a lock.
 * The record is dedicated for reading the state of the lock from a persistent
 * store.
 *
 * @param lockKey           The key of the lock.
 * @param lockHandleId      The handle ID of the lock.
 * @param createdTime       The time when the lock was created.
 * @param expirationSeconds The number of seconds after which the lock will
 *                          expire.
 * @param currentTime       The current time for checking lock expiration.
 */
public record ReadLockRecord(String lockKey,
        String lockHandleId,
        LocalDateTime createdTime,
        long expirationSeconds,
        LocalDateTime currentTime) {

    public static ReadLockRecord of(WriteLockRecord lockRecord, LocalDateTime currentTime) {
        return new ReadLockRecord(
                lockRecord.lockKey(),
                lockRecord.lockHandleId(),
                lockRecord.createdTime(),
                lockRecord.expirationSeconds(),
                currentTime);
    }
}
