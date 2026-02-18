package com.dlock.api;

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
     * Gets a lock for a given amount of time, if available (providing the handle of
     * that lock).
     * If the lock is taken by someone there is no exception thrown but simply
     * {@link Optional#empty} is returned.
     *
     * @throws com.dlock.api.exception.LockException if an unexpected error occurs during lock acquisition
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
     * @throws com.dlock.api.exception.LockException if an unexpected error occurs during lock acquisition
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
     * Tries to acquire a lock and, if successful, executes the given function.
     * The lock is automatically released after the function completes (or throws).
     * If the lock is not available, the function is not executed and {@link Optional#empty()} is returned.
     *
     * @param lockKey           the key identifying the lock
     * @param expirationSeconds the lock expiration time in seconds
     * @param action            the function to execute while holding the lock
     * @param <R>               the return type of the action
     * @return an {@link Optional} containing the result of the action if the lock was acquired, or empty otherwise
     * @throws com.dlock.api.exception.LockException if an unexpected error occurs during lock acquisition
     */
    default <R> Optional<R> tryLock(String lockKey, long expirationSeconds, Function<LockHandle, R> action) {
        Optional<LockHandle> lock = tryLock(lockKey, expirationSeconds);
        if (lock.isPresent()) {
            try {
                return Optional.ofNullable(action.apply(lock.get()));
            } finally {
                unlock(lock.get());
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Releases a given lock. If lock with a given handle does not exist nothings
     * happen.
     */
    void unlock(LockHandle lockHandle);

}
