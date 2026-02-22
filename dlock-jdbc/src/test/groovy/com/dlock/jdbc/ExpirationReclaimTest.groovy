package io.github.pmalirz.dlock.jdbc

import io.github.pmalirz.dlock.core.SimpleKeyLock
import io.github.pmalirz.dlock.jdbc.builder.JDBCKeyLockBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import spock.lang.Specification

/**
 * Verifies that an expired lock is correctly reclaimed by a subsequent tryLock call.
 * The lock is created with a very short expiration, then after waiting for it to expire,
 * a new tryLock call should succeed and produce a new handle.
 *
 * Uses H2 in-memory database.
 *
 * @author Przemyslaw Malirz
 */
class ExpirationReclaimTest extends Specification {

    private static final String LOCK_KEY = "expiration-test-key"

    private SimpleKeyLock keyLock

    def setup() {
        def config = new HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:expirationTest;DB_CLOSE_DELAY=-1"
        config.username = "sa"
        def dataSource = new HikariDataSource(config)

        keyLock = new JDBCKeyLockBuilder()
                .dataSource(dataSource)
                .databaseType(DatabaseType.H2)
                .createDatabase(true)
                .build()

        // Purge table before each test
        dataSource.connection.prepareStatement("DELETE FROM DLCK").executeUpdate()
    }

    def "Expired lock is reclaimed by a new tryLock"() {
        given: "A lock acquired with a 1-second expiration"
        def originalHandle = keyLock.tryLock(LOCK_KEY, 1)
        assert originalHandle.present

        when: "We wait for the lock to expire and try again"
        // H2 CURRENT_TIMESTAMP is used for expiration checks.
        // Sleep slightly longer than expiration to ensure it has expired.
        Thread.sleep(2000)
        def reclaimedHandle = keyLock.tryLock(LOCK_KEY, 300)

        then: "A new lock is acquired with a different handle"
        reclaimedHandle.present
        reclaimedHandle.get().handleId() != originalHandle.get().handleId()
    }

    def "Non-expired lock cannot be reclaimed"() {
        given: "A lock acquired with a long expiration"
        def originalHandle = keyLock.tryLock(LOCK_KEY, 300)
        assert originalHandle.present

        when: "We immediately try to acquire the same key"
        def secondAttempt = keyLock.tryLock(LOCK_KEY, 300)

        then: "The second attempt fails"
        !secondAttempt.present
    }

    def "Reclaimed lock can be unlocked normally"() {
        given: "A lock that has expired and been reclaimed"
        keyLock.tryLock(LOCK_KEY, 1)
        Thread.sleep(2000)
        def reclaimedHandle = keyLock.tryLock(LOCK_KEY, 300)
        assert reclaimedHandle.present

        when: "We unlock the reclaimed lock"
        keyLock.unlock(reclaimedHandle.get())
        def freshHandle = keyLock.tryLock(LOCK_KEY, 300)

        then: "The key is free again"
        freshHandle.present
    }
}
