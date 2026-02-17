package com.dlock.api;

import java.util.Optional;
import java.util.function.Consumer;

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
     * Tries to acquire a lock and, if successful, executes the given action.
     * The lock is automatically released after the action completes (or throws).
     * If the lock is not available, the action is not executed.
     *
     * @param lockKey           the key identifying the lock
     * @param expirationSeconds the lock expiration time in seconds
     * @param action            the action to execute while holding the lock
     */
    default void tryLock(String lockKey, long expirationSeconds, Consumer<LockHandle> action) {
        Optional<LockHandle> lock = tryLock(lockKey, expirationSeconds);
        if (lock.isPresent()) {
            try {
                action.accept(lock.get());
            } finally {
                unlock(lock.get());
            }
        }
    }

    /**
     * Releases a given lock. If lock with a given handle does not exist nothings
     * happen.
     */
    void unlock(LockHandle lockHandle);

}
