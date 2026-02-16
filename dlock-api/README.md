# dlock-api

This module provides the core API contract for **dlock**, ensuring minimal coupling between your application and the lock implementation details.

## Key Interfaces

### KeyLock

The primary interface for simplified lock operations.

```kotlin
interface KeyLock {
    /**
     * Attempts to acquire a lock by key.
     * @return Optional<LockHandle> - Present if acquired, empty if not.
     */
    fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle>

    /**
     * Releases the lock using the provided handle.
     */
    fun unlock(lockHandle: LockHandle)
}
```

### LockHandle

Represents an active lock. It contains the key and the handle ID.

```kotlin
interface LockHandle {
    val handleId: String
    val key: String
}
```

## Usage

In your application code, depend only on `dlock-api` to keep your codebase clean and testable, while configuring the implementation (`dlock-jdbc`, `dlock-core`) at runtime via DI.
