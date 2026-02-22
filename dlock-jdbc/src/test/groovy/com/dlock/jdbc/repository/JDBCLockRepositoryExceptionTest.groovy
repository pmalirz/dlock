package io.github.pmalirz.dlock.jdbc.repository

import io.github.pmalirz.dlock.api.exception.LockException
import io.github.pmalirz.dlock.core.model.WriteLockRecord
import io.github.pmalirz.dlock.jdbc.tool.script.ScriptResolver
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.SQLException
import java.time.LocalDateTime

class JDBCLockRepositoryExceptionTest extends Specification {

    JDBCLockRepository repository
    DataSource dataSource = Mock()
    ScriptResolver scriptResolver = Mock()

    def setup() {
        scriptResolver.resolveScript(_) >> "SQL"
        repository = new JDBCLockRepository(scriptResolver, dataSource)
    }

    def "createLock should throw LockException when SQLException occurs"() {
        given:
        dataSource.getConnection() >> { throw new SQLException("DB error") }
        def record = new WriteLockRecord("key", "handle", LocalDateTime.now(), 10)

        when:
        repository.createLock(record)

        then:
        thrown(LockException)
    }

    def "findLockByHandleId should throw LockException when SQLException occurs"() {
        given:
        dataSource.getConnection() >> { throw new SQLException("DB error") }

        when:
        repository.findLockByHandleId("handle")

        then:
        thrown(LockException)
    }

    def "findLockByKey should throw LockException when SQLException occurs"() {
        given:
        dataSource.getConnection() >> { throw new SQLException("DB error") }

        when:
        repository.findLockByKey("key")

        then:
        thrown(LockException)
    }

    def "removeLock should throw LockException when SQLException occurs"() {
        given:
        dataSource.getConnection() >> { throw new SQLException("DB error") }

        when:
        repository.removeLock("handle")

        then:
        thrown(LockException)
    }
}
