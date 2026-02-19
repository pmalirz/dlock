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

    def "should execute all ddls even if one fails"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ...", "CREATE INDEX ..."]
        dataSource.getConnection() >> connection
        connection.createStatement() >> statement

        when:
        initDatabase.createDatabase()

        then:
        1 * statement.execute("CREATE TABLE DLCK ...") >> { throw new SQLException("Table exists") }
        1 * statement.execute("CREATE INDEX ...")
        noExceptionThrown()
    }

    def "should throw exception if connection fails"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        dataSource.getConnection() >> { throw new SQLException("Connection failed") }

        when:
        initDatabase.createDatabase()

        then:
        thrown(RuntimeException)
    }
}
