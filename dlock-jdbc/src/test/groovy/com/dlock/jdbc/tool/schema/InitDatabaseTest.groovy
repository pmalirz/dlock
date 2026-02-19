package com.dlock.jdbc.tool.schema

import com.dlock.jdbc.tool.script.ScriptResolver
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class InitDatabaseTest extends Specification {

    ScriptResolver scriptResolver = Mock()
    DataSource dataSource = Mock()
    Connection connection = Mock()
    Statement statement = Mock()

    def "should ignore Oracle object already exists error"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        dataSource.getConnection() >> connection
        connection.createStatement() >> statement

        and:
        statement.execute(_) >> { throw new SQLException("Object already exists", "42000", 955) }

        when:
        initDatabase.createDatabase()

        then:
        noExceptionThrown()
    }

    def "should throw exception for other errors"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        dataSource.getConnection() >> connection
        connection.createStatement() >> statement

        and:
        statement.execute(_) >> { throw new SQLException("Syntax error", "42000", 123) }

        when:
        initDatabase.createDatabase()

        then:
        thrown(RuntimeException)
    }
}
