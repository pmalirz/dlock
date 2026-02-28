# dlock-spring

This module provides Spring Framework integration for **dlock** via the `@Lock` annotation.

## Features

* **Declarative Locking**: Annotate methods with `@Lock` to create a distributed lock around their execution.
* **Parameter Binding**: Use `@LockKeyParam` to dynamically construct lock keys from method arguments.
* **Skip-if-Locked**: If the lock is already held by another process, the method execution is skipped entirely.

## Configuration

To use `@Lock`, you must configure:

1. A `KeyLock` bean (the lock implementation).
2. Enable component scanning for `io.github.pmalirz.dlock` (so the Aspect is detected).

```java
@Configuration
@ComponentScan("io.github.pmalirz.dlock")
public class DLockConfig {

    @Bean
    public KeyLock keyLock(DataSource dataSource) {
        return new JDBCKeyLockBuilder()
                .dataSource(dataSource)
                .databaseType(DatabaseType.H2) // or POSTGRESQL, ORACLE
                .createDatabase(true) // Automatically creates the DLCK table
                .build();
    }
}
```

## Usage

### Basic Usage

```java
@Lock(key = "daily-report", expirationSeconds = 300)
public void generateDailyReport() {
    // This runs only if "daily-report" lock is acquired.
}
```

### Dynamic Keys

Use `@LockKeyParam` to include method arguments in the lock key.

```java
@Lock(key = "user-update-{userId}", expirationSeconds = 60)
public void updateUser(@LockKeyParam("userId") String userId, UserData data) {
    // Lock key will be e.g., "user-update-12345"
}
```

## Important Notes

1. **Return Values**: If the lock is acquired, the method executes and returns its value. If the lock is **not** acquired, the execution is skipped.
   - If the method returns `Optional<T>`, `Optional.empty()` is returned.
   - If the method returns a primitive type (except `void`), a `LockException` is thrown because `null` cannot be returned.
   - Otherwise, `null` is returned. Callers should be prepared to handle `null`.
2. **Skipping**: If the lock is not acquired, the method body is **not executed**.
3. **Self-Invocation**: Due to Spring AOP proxy mechanism, calling an `@Lock` method from within the same class will bypass the aspect (and the lock).
