package io.github.pmalirz.dlock.jdbc.builder;

import io.github.pmalirz.dlock.core.SimpleKeyLock;
import io.github.pmalirz.dlock.core.expiration.LocalLockExpirationPolicy;
import io.github.pmalirz.dlock.core.expiration.LockExpirationPolicy;
import io.github.pmalirz.dlock.core.handle.LockHandleIdGenerator;
import io.github.pmalirz.dlock.core.handle.LockHandleUUIDIdGenerator;
import io.github.pmalirz.dlock.core.util.time.DateTimeProvider;
import io.github.pmalirz.dlock.jdbc.DatabaseType;
import io.github.pmalirz.dlock.jdbc.repository.JDBCLockRepository;
import io.github.pmalirz.dlock.jdbc.tool.schema.InitDatabase;
import io.github.pmalirz.dlock.jdbc.tool.script.ScriptResolver;

import javax.sql.DataSource;

/**
 * Builder for {@link SimpleKeyLock} backed by the JDBC repository (database).
 */
public class JDBCKeyLockBuilder {

    public static final String DEFAULT_LOCK_TABLE_NAME = "DLCK";

    private DataSource dataSource;
    private DatabaseType databaseType;
    private String lockTableName = DEFAULT_LOCK_TABLE_NAME;
    private LockHandleIdGenerator lockHandleIdGenerator = new LockHandleUUIDIdGenerator();
    private LockExpirationPolicy lockExpirationPolicy = new LocalLockExpirationPolicy();
    private DateTimeProvider lockDateTimeProvider = DateTimeProvider.SYSTEM;
    private boolean createDatabase = false;

    /**
     * That is the only required property.
     */
    public JDBCKeyLockBuilder dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public JDBCKeyLockBuilder databaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public JDBCKeyLockBuilder lockTableName(String lockTableName) {
        if (lockTableName == null || !lockTableName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Table name must only contain alphanumeric characters and underscores");
        }
        this.lockTableName = lockTableName;
        return this;
    }

    public JDBCKeyLockBuilder lockHandleIdGenerator(LockHandleIdGenerator lockHandleIdGenerator) {
        this.lockHandleIdGenerator = lockHandleIdGenerator;
        return this;
    }

    public JDBCKeyLockBuilder lockExpirationPolicy(LockExpirationPolicy lockExpirationPolicy) {
        this.lockExpirationPolicy = lockExpirationPolicy;
        return this;
    }

    public JDBCKeyLockBuilder lockDateTimeProvider(DateTimeProvider lockDateTimeProvider) {
        this.lockDateTimeProvider = lockDateTimeProvider;
        return this;
    }

    public JDBCKeyLockBuilder createDatabase(boolean createDatabase) {
        this.createDatabase = createDatabase;
        return this;
    }

    public SimpleKeyLock build() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is required");
        }
        if (databaseType == null) {
            throw new IllegalStateException("DatabaseType is required");
        }

        ScriptResolver scriptResolver = new ScriptResolver(databaseType, lockTableName);

        JDBCLockRepository lockRepository = new JDBCLockRepository(scriptResolver, dataSource);
        SimpleKeyLock dbdLock = new SimpleKeyLock(lockRepository, lockHandleIdGenerator, lockExpirationPolicy,
                lockDateTimeProvider);

        if (createDatabase) {
            new InitDatabase(scriptResolver, dataSource).createDatabase();
        }

        return dbdLock;
    }

}
