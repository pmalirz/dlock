# dlock-api

This module provides the core API contract for **dlock**, ensuring minimal coupling between your application and the lock implementation details.

## Key Interfaces

### KeyLock

The primary interface for simplified lock operations.

```java
public interface KeyLock {
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
        // ...
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
        // ...
    }

    /**
     * Releases a given lock. If lock with a given handle does not exist nothing
     * happens. If the given handle is null, it should safely return.
     */
    void unlock(LockHandle lockHandle);
}
```

### LockHandle

Represents an active lock. It contains the handle ID.

```java
public record LockHandle(String handleId) {
}
```

## Usage

In your application code, depend only on `dlock-api` to keep your codebase clean and testable, while configuring the implementation (`dlock-jdbc`, `dlock-core`) at runtime via DI.
