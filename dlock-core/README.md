# dlock-core

This module contains the core implementation logic for **dlock**, independent of specific storage mechanisms (like JDBC).

## Components

* **SimpleKeyLock**: The main implementation of `KeyLock` interface. It orchestrates the locking process using a `LockRepository`.
* **LockRepository**: Interface for storage backends (e.g., `JDBCLockRepository` implements this).
* **LockExpirationPolicy**: Strategy for handling lock expiration. Defaults to `LocalLockExpirationPolicy`.
* **LockHandleIdGenerator**: Strategy for generating unique lock handles. Defaults to UUID.

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
    public boolean tryLock(LockModel lock) {
        // Implement lock acquisition
        return false;
    }

    @Override
    public boolean unlock(LockModel lock) {
        // Implement release
        return false;
    }
}

SimpleKeyLock myLock = new SimpleKeyLock(new MyCustomRepository(), ...);
```
