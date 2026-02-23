package io.github.pmalirz.dlock;

import io.github.pmalirz.dlock.api.KeyLock;
import io.github.pmalirz.dlock.jdbc.DatabaseType;
import io.github.pmalirz.dlock.jdbc.builder.JDBCKeyLockBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KeyLockNoCollisionH2Benchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        HikariDataSource dataSource;
        KeyLock keyLock;
        Server h2Server;

        @Setup(Level.Trial)
        public void setup() throws SQLException {
            h2Server = Server.createTcpServer("-ifNotExists", "-tcp", "-tcpAllowOthers", "-tcpPort", "9079");
            h2Server.start();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:tcp://localhost:9079/~/dlockjmh");
            config.setUsername("sa");
            config.setPassword("");
            config.setAutoCommit(true);
            config.addDataSourceProperty("maximumPoolSize", "1000");
            dataSource = new HikariDataSource(config);

            keyLock = new JDBCKeyLockBuilder().dataSource(dataSource)
                    .databaseType(DatabaseType.H2)
                    .createDatabase(true).build();
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (dataSource != null) {
                dataSource.close();
            }
            if (h2Server != null) {
                h2Server.stop();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void tryLockNoCollision(ExecutionPlan executionPlan, Blackhole bh) {
        bh.consume(executionPlan.keyLock.tryLock(UUID.randomUUID().toString(), 1));
    }
}
