# dlock-api

This module provides the core API contract for **dlock**, ensuring minimal coupling between your application and the lock implementation details.

## Key Interfaces

### KeyLock

The primary interface for simplified lock operations.

```java
public interface KeyLock {
    /**
     * Attempts to acquire a lock by key.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max KeyLock.MAX_LOCK_KEY_LENGTH characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @return Optional<LockHandle> - Present if acquired, empty if not.
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    Optional<LockHandle> tryLock(String lockKey, long expirationSeconds);

    /**
     * Tries to acquire a lock and, if successful, executes the given action.
     * The lock is automatically released after the action completes.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max KeyLock.MAX_LOCK_KEY_LENGTH characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    default void tryLock(String lockKey, long expirationSeconds, Consumer<LockHandle> action) {
        // ...
    }

    /**
     * Tries to acquire a lock and, if successful, executes the given function.
     * The lock is automatically released after the function completes.
     *
     * @param lockKey           the key identifying the lock (must be non-blank and max KeyLock.MAX_LOCK_KEY_LENGTH characters)
     * @param expirationSeconds the lock expiration time in seconds (must be greater than 0)
     * @return Optional<R> - Result of the function if lock acquired, empty if not.
     * @throws IllegalArgumentException if lockKey is invalid or expirationSeconds is <= 0
     */
    default <R> Optional<R> tryLock(String lockKey, long expirationSeconds, Function<LockHandle, R> action) {
        // ...
    }

    /**
     * Releases the lock using the provided handle.
     * If the lockHandle is null, it safely returns early.
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
