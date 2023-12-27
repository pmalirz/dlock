package com.dlock.jdbc

import com.dlock.api.LockHandle
import com.dlock.core.SimpleKeyLock
import com.dlock.jdbc.builder.JDBCKeyLockBuilder
import com.dlock.jdbc.repository.JDBCLockRepository
import com.dlock.jdbc.tool.script.ScriptResolver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import spock.lang.Specification

/**
 * Bunch of quick tests against SimpleKeyLock. They cover most of the cases we are interested in.
 * There is also dlock-benchmark (based on jmh) project which completes that integration test with more
 * concurrent usage scenarios.
 *
 * The test requires TestRuntimeProperties passed in runtime to work with other database than H2.
 * That way we can test compatibility with other SQL database engines.
 *
 * @author Przemyslaw Malirz
 */
class JDBCSimpleKeyLockTest extends Specification {

    private SimpleKeyLock keyLock
    private JDBCLockRepository repository

    def setup() {
        def testProperties = TestRuntimeProperties.getProperties()

        def config = new HikariConfig()
        config.jdbcUrl = testProperties.getDatabaseURL()
        config.username = testProperties.getDatabaseUser()
        config.password = testProperties.getDatabasePassword()
        def dataSource = new HikariDataSource(config)

        def scriptResolver = new ScriptResolver(testProperties.getDatabaseType(), JDBCKeyLockBuilder.DEFAULT_LOCK_TABLE_NAME)
        // it help us to check if the lock exists in the database once created
        repository = new JDBCLockRepository(scriptResolver, dataSource)

        keyLock = new JDBCKeyLockBuilder()
                .dataSource(dataSource)
                .databaseType(testProperties.getDatabaseType())
                .createDatabase(testProperties.getDatabaseInit())
                .build()

        // let's purge DLOCK table just to be sure nothing interferes with the test suit
        dataSource.connection.prepareStatement("DELETE FROM DLCK").executeUpdate()
    }

    def "Try Lock"() {
        when:
        def lockHandle = keyLock.tryLock("a", 300)

        then:
        noExceptionThrown()
        assert lockHandle.present

        def createdLock = repository.findLockByKey("a")
        assert createdLock.lockHandleId == lockHandle.get().handleId
    }

    def "Release Lock"() {
        when:
        def lockHandle = keyLock.tryLock("b", 300)
        keyLock.unlock(lockHandle.get())

        then:
        noExceptionThrown()

        def createdLock = repository.findLockByKey("b")
        assert createdLock == null
    }

    def "Release not existing Lock"() {
        when:
        keyLock.unlock new LockHandle("xyz")

        then:
        noExceptionThrown()
    }

    def "Collision Lock"() {
        when:
        def lockHandleA = keyLock.tryLock("c", 10)
        def lockHandleB = keyLock.tryLock("c", 10)

        then:
        noExceptionThrown()

        assert lockHandleA.present
        assert !lockHandleB.present
    }

}
