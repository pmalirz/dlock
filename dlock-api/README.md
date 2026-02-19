# dlock-api

This module provides the core API contract for **dlock**, ensuring minimal coupling between your application and the lock implementation details.

## Key Interfaces

### KeyLock

The primary interface for simplified lock operations.

```java
public interface KeyLock {
    /**
     * Attempts to acquire a lock by key.
     * @return Optional<LockHandle> - Present if acquired, empty if not.
     */
    Optional<LockHandle> tryLock(String lockKey, long expirationSeconds);

    /**
     * Tries to acquire a lock and, if successful, executes the given action.
     * The lock is automatically released after the action completes.
     */
    void tryLock(String lockKey, long expirationSeconds, Consumer<LockHandle> action);

    /**
     * Tries to acquire a lock and, if successful, executes the given function.
     * The lock is automatically released after the function completes.
     * @return Optional<R> - Result of the function if lock acquired, empty if not.
     */
    <R> Optional<R> tryLock(String lockKey, long expirationSeconds, Function<LockHandle, R> action);

    /**
     * Releases the lock using the provided handle.
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
