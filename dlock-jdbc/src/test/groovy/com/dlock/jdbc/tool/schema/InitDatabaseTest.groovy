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

    def "should throw exception if SQL fails"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        dataSource.getConnection() >> connection
        connection.createStatement() >> statement
        statement.execute(_) >> { throw new SQLException("Table exists") }

        when:
        initDatabase.createDatabase()

        then:
        thrown(RuntimeException)
    }

    def "should execute DDL successfully"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        dataSource.getConnection() >> connection
        connection.createStatement() >> statement

        when:
        initDatabase.createDatabase()

        then:
        1 * statement.execute("CREATE TABLE DLCK ...")
        noExceptionThrown()
    }
}
