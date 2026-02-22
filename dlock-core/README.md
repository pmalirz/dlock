# dlock-core

This module contains the core implementation logic for **dlock**, independent of specific storage mechanisms (like JDBC).

## Components

* **SimpleKeyLock**: The main implementation of `KeyLock` interface. It orchestrates the locking process using a `LockRepository`.
* **LockRepository**: Interface for storage backends (e.g., `JDBCLockRepository` implements this).
* **LockExpirationPolicy**: Strategy for handling lock expiration. Defaults to `LocalLockExpirationPolicy`.
* **LockHandleIdGenerator**: Strategy for generating unique lock handles. Defaults to UUID.
* **DateTimeProvider**: Interface for providing current time (for testing and consistency). Defaults to system time.

## Architecture

`dlock` separates the locking logic from the storage logic.

1. **KeyLock** (API) -> calls -> **SimpleKeyLock** (Core)
2. **SimpleKeyLock** -> calls -> **LockRepository** (Storage Implementation)

This design allows for easy extension to other storage backends (e.g., Redis, Mongo) by simply implementing `LockRepository`.

## Extension Points

To implement a custom storage backend:

1. Implement `LockRepository`.
2. Instantiate `SimpleKeyLock` with your repository.

```java
public class MyCustomRepository implements LockRepository {
    @Override
    public boolean createLock(WriteLockRecord lockRecord) {
        // Implement lock acquisition (e.g. INSERT)
        // Return true if successful, false if key already exists
        return false;
    }

    @Override
    public ReadLockRecord findLockByHandleId(String lockHandleId) {
        // Find lock by handle ID
        return null;
    }

    @Override
    public ReadLockRecord findLockByKey(String lockKey) {
        // Find lock by key
        return null;
    }

    @Override
    public void removeLock(String lockHandleId) {
        // Remove lock by handle ID
    }
}

SimpleKeyLock myLock = new SimpleKeyLock(new MyCustomRepository(), ...);
```
