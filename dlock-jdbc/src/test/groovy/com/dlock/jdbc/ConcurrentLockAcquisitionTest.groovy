package com.dlock.jdbc

import com.dlock.api.LockHandle
import com.dlock.core.SimpleKeyLock
import com.dlock.jdbc.builder.JDBCKeyLockBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import spock.lang.Specification

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Verifies that only one thread out of N concurrent threads can acquire
 * a lock for the same key, and all others are gracefully rejected.
 *
 * Uses H2 in-memory database.
 *
 * @author Przemyslaw Malirz
 */
class ConcurrentLockAcquisitionTest extends Specification {

    private static final int THREAD_COUNT = 20
    private static final String LOCK_KEY = "concurrent-test-key"
    private static final long EXPIRATION_SECONDS = 300

    private SimpleKeyLock keyLock

    def setup() {
        def config = new HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:concurrentTest;DB_CLOSE_DELAY=-1"
        config.username = "sa"
        config.maximumPoolSize = THREAD_COUNT + 5
        def dataSource = new HikariDataSource(config)

        keyLock = new JDBCKeyLockBuilder()
                .dataSource(dataSource)
                .databaseType(DatabaseType.H2)
                .createDatabase(true)
                .build()

        // Purge table before each test
        dataSource.connection.prepareStatement("DELETE FROM DLCK").executeUpdate()
    }

    def "Only one thread acquires the lock out of N concurrent threads"() {
        given:
        def executor = Executors.newFixedThreadPool(THREAD_COUNT)
        def barrier = new CyclicBarrier(THREAD_COUNT) // ensures all threads start at the same time

        when:
        List<Future<Optional<LockHandle>>> futures = (1..THREAD_COUNT).collect {
            executor.submit({
                barrier.await(5, TimeUnit.SECONDS) // wait for all threads to be ready
                return keyLock.tryLock(LOCK_KEY, EXPIRATION_SECONDS)
            } as java.util.concurrent.Callable<Optional<LockHandle>>)
        }

        def results = futures.collect { it.get(10, TimeUnit.SECONDS) }
        def successfulLocks = results.findAll { it.present }
        def failedLocks = results.findAll { !it.present }

        then:
        successfulLocks.size() == 1
        failedLocks.size() == THREAD_COUNT - 1

        cleanup:
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }

    def "After lock release, another thread can acquire the same key"() {
        given:
        def lockHandle = keyLock.tryLock(LOCK_KEY, EXPIRATION_SECONDS)

        when:
        assert lockHandle.present
        keyLock.unlock(lockHandle.get())
        def newLockHandle = keyLock.tryLock(LOCK_KEY, EXPIRATION_SECONDS)

        then:
        newLockHandle.present
        newLockHandle.get().handleId() != lockHandle.get().handleId()
    }
}
