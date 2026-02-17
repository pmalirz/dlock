package com.dlock;

import com.dlock.api.KeyLock;
import com.dlock.jdbc.DatabaseType;
import com.dlock.jdbc.builder.JDBCKeyLockBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Test with Oracle.
 *
 * @author Przemyslaw Malirz
 */
public class KeyLockCollisionOracleBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        KeyLock keyLock;
        final String LOCK_KEY = "AAA";

        @Setup(Level.Iteration)
        public void setUp() {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:oracle:thin:@localhost:1521:XE");
            config.setUsername("dlock");
            config.setPassword("dlock");
            config.setAutoCommit(true);
            config.addDataSourceProperty("maximumPoolSize", "1000");
            HikariDataSource dataSource = new HikariDataSource(config);

            keyLock = new JDBCKeyLockBuilder().dataSource(dataSource)
                    .databaseType(DatabaseType.ORACLE)
                    .createDatabase(false).build();

            keyLock.tryLock(LOCK_KEY, 100000);
        }
    }

    // @Benchmark
    // @BenchmarkMode(Mode.Throughput)
    // @OutputTimeUnit(TimeUnit.SECONDS)
    public void tryLockAlwaysCollision(ExecutionPlan executionPlan, Blackhole bh) {
        bh.consume(executionPlan.keyLock.tryLock(executionPlan.LOCK_KEY, 1));
    }
}
