package io.github.pmalirz.dlock.api;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * KeyLock is the main interface of dlock. It represents the main API of the
 * library.
 * KeyLock's implementation must be thread-safe.
 *
 * @author Przemyslaw Malirz
 */
public interface KeyLock {

    /**
     * Maximum allowed length for a lock key.
     * This limit ensures compatibility with the underlying database schema.
     */
    int MAX_LOCK_KEY_LENGTH = 1000;

    /**
     * Gets a lock for a given amount of time, if available (providing the handle of
     * that lock).
     * If the lock is taken by someone there is no exception thrown but simply
     * {@link Optional#empty} is returned.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max {@value #MAX_LOCK_KEY_LENGTH} characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @throws io.github.pmalirz.dlock.api.exception.LockException if an unexpected error occurs
     *                                               during lock acquisition
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    Optional<LockHandle> tryLock(String lockKey, long expirationSeconds);

    /**
     * Tries to acquire a lock and, if successful, executes the given action.
     * The lock is automatically released after the action completes (or throws).
     * If the lock is not available, the action is not executed.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max {@value #MAX_LOCK_KEY_LENGTH} characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @param action            the action to execute while holding the lock
     * @throws io.github.pmalirz.dlock.api.exception.LockException if an unexpected error occurs
     *                                               during lock acquisition
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    default void tryLock(String lockKey, long expirationSeconds, Consumer<LockHandle> action) {
        Optional<LockHandle> lock = tryLock(lockKey, expirationSeconds);
        if (lock.isPresent()) {
            LockHandle lockHandle = lock.get();
            try {
                action.accept(lockHandle);
            } finally {
                unlock(lockHandle);
            }
        }
    }

    /**
     * Tries to acquire a lock and, if successful, executes the given function.
     * The lock is automatically released after the function completes (or throws).
     * If the lock is not available, the function is not executed and
     * {@link Optional#empty()} is returned.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max {@value #MAX_LOCK_KEY_LENGTH} characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @param action            the function to execute while holding the lock
     * @param <R>               the return type of the action
     * @return an {@link Optional} containing the result of the action if the lock
     *         was acquired, or empty otherwise
     * @throws io.github.pmalirz.dlock.api.exception.LockException if an unexpected error occurs
     *                                               during lock acquisition
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    default <R> Optional<R> tryLock(String lockKey, long expirationSeconds, Function<LockHandle, R> action) {
        Optional<LockHandle> lock = tryLock(lockKey, expirationSeconds);
        if (lock.isPresent()) {
            LockHandle lockHandle = lock.get();
            try {
                return Optional.ofNullable(action.apply(lockHandle));
            } finally {
                unlock(lockHandle);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Releases a given lock. If lock with a given handle does not exist nothing
     * happens. If the given handle is null, it should safely return.
     */
    void unlock(LockHandle lockHandle);

}
