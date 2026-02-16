package com.dlock.api;

import java.util.Optional;

/**
 * KeyLock is the main interface of dlock. It represents the main API of the
 * library.
 * KeyLock's implementation must be thread-safe.
 *
 * @author Przemyslaw Malirz
 */
public interface KeyLock {

    /**
     * Gets a lock for a given amount of time, if available (providing the handle of
     * that lock).
     * If the lock is taken by someone there is no exception thrown but simply
     * {@link Optional#empty} is returned.
     */
    Optional<LockHandle> tryLock(String lockKey, long expirationSeconds);

    /**
     * Releases a given lock. If lock with a given handle does not exist nothings
     * happen.
     */
    void unlock(LockHandle lockHandle);

}
