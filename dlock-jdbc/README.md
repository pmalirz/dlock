# dlock-jdbc

This module provides the JDBC implementation of the **dlock** repository. It persists locks in a relational database table (default: `DLCK`).

## Features

* **ACID Compliance**: Uses database transactions to ensure lock consistency.
* **Simple Schema**: Requires only a single table with 4 columns.
* **Extensible**: Comes with built-in support for H2, PostgreSQL and Oracle, but is designed to be adaptable to other SQL dialects.

## Supported Databases

Out-of-the-box support is provided for:

* **H2** (Memory/File) - Great for testing and development.
* **Oracle** - Production-grade support.
* **PostgreSQL** - Production-grade support.

To support other databases, valid SQL scripts must be provided for the `ScriptResolver`.

## Safety Guarantees

**dlock-jdbc guarantees mutual exclusion**: only one process can hold a given lock at any time, even across multiple JVMs and nodes. This is ensured by three independent layers of protection in the `tryLock` flow:

### Three-Layer Defense

| Layer | Mechanism | What It Prevents |
| :--- | :--- | :--- |
| 1. **DELETE by handle ID** | Expired locks are removed using their unique `LCK_HNDL_ID` | Cannot accidentally delete another process's newly acquired lock |
| 2. **Conditional INSERT** | `INSERT...SELECT...WHERE NOT EXISTS` (H2, Oracle) or `ON CONFLICT DO NOTHING` (PostgreSQL) | Prevents inserting a duplicate lock if another process just acquired it |
| 3. **PRIMARY KEY on `LCK_KEY`** | Database-enforced unique constraint | Ultimate safety net — the database itself rejects any double-insert |

### Concurrent Expiration Reclaim

When two processes attempt to reclaim an expired lock simultaneously:

1. Both find the expired lock and note its `handleId` (e.g., `"old-handle"`)
2. **Process A** deletes `"old-handle"` and inserts a new lock → **succeeds**
3. **Process B** tries to delete `"old-handle"` → **no-op** (already deleted by A), and B's INSERT fails because A's row already exists for the same `LCK_KEY`

Result: exactly one process wins. No transaction wrapping is required — the safety comes from the SQL design itself.

## Usage

Unless you are building a custom integration, you will typically use this via the `JDBCKeyLockBuilder`.

```java
KeyLock keyLock = new JDBCKeyLockBuilder()
    .dataSource(myDataSource)
    .databaseType(DatabaseType.H2)
    .createDatabase(true) // Automatically creates the DLCK table if not exists
    .build();
```

## Benchmarks

The following benchmarks were run to test performance and overhead.

Run them yourself:

```bash
./gradlew :dlock-jdbc:jmh
```

### Sample Results (Dell XPS 9560)

| Benchmark | Mode | Score | Units |
| :--- | :--- | :--- | :--- |
| `tryAndReleaseLockNoCollision` | thrpt | ~5,182 | ops/ms |
| `tryLockAlwaysCollision` | thrpt | ~32 | ops/ms |
| `tryLockNoCollision` | thrpt | ~6 | ops/ms |

*Note: `tryLockAlwaysCollision` is lower because it involves waiting/failing on unique constraint violations.*

<details>
<summary>Full Raw Benchmark Data</summary>

```text
Benchmark                                                                                               Mode  Cnt       Score   Error   Units
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision                                   thrpt            5,182          ops/ms
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:·gc.alloc.rate                    thrpt           35,725          MB/sec
KeyLockAndReleaseNoCollisionH2Benchmark.tryAndReleaseLockNoCollision:·gc.time                          thrpt          304,000              ms
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision                                                     thrpt           32,123          ops/ms
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:·gc.alloc.rate                                      thrpt           87,445          MB/sec
KeyLockCollisionH2Benchmark.tryLockAlwaysCollision:·gc.time                                            thrpt           17,000              ms
KeyLockNoCollisionH2Benchmark.tryLockNoCollision                                                       thrpt            6,931          ops/ms
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:·gc.alloc.rate                                        thrpt           38,352          MB/sec
KeyLockNoCollisionH2Benchmark.tryLockNoCollision:·gc.time                                              thrpt          177,000              ms
```

*Raw output typically spans many more metrics.*
</details>
