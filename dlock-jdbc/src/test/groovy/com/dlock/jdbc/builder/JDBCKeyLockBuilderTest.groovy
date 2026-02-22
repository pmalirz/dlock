package io.github.pmalirz.dlock.jdbc.builder

import spock.lang.Specification
import spock.lang.Unroll

class JDBCKeyLockBuilderTest extends Specification {

    @Unroll
    def "should accept valid lockTableName: #tableName"() {
        given:
        def builder = new JDBCKeyLockBuilder()

        when:
        builder.lockTableName(tableName)

        then:
        noExceptionThrown()

        where:
        tableName << ["DLCK", "MyTable_1", "My_Table"]
    }

    @Unroll
    def "should reject invalid lockTableName: #tableName"() {
        given:
        def builder = new JDBCKeyLockBuilder()

        when:
        builder.lockTableName(tableName)

        then:
        thrown(IllegalArgumentException)

        where:
        tableName << ["My-Table", "My.Table", "My Table", "DROP TABLE", ""]
    }

    def "should reject null lockTableName"() {
        given:
        def builder = new JDBCKeyLockBuilder()

        when:
        builder.lockTableName(null)

        then:
        thrown(IllegalArgumentException)
    }
}
