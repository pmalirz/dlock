package com.dlock.jdbc.tool.schema

import com.dlock.jdbc.tool.script.ScriptResolver
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class InitDatabaseTest extends Specification {

    ScriptResolver scriptResolver = Mock()
    DataSource dataSource = Mock()
    Connection connection = Mock()
    Statement statement = Mock()
    DatabaseMetaData metaData = Mock()
    ResultSet resultSet = Mock()

    def setup() {
        dataSource.getConnection() >> connection
        connection.getMetaData() >> metaData
        metaData.getTables(_, _, _, _) >> resultSet
        scriptResolver.getTableName() >> "DLCK"
    }

    def "should throw exception if SQL fails"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        connection.createStatement() >> statement
        resultSet.next() >> false // Table does not exist
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
        connection.createStatement() >> statement
        resultSet.next() >> false // Table does not exist

        when:
        initDatabase.createDatabase()

        then:
        1 * statement.execute("CREATE TABLE DLCK ...")
        noExceptionThrown()
    }

    def "should skip DDL if table exists"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        resultSet.next() >> true // Table exists

        when:
        initDatabase.createDatabase()

        then:
        0 * scriptResolver.resolveDDLScripts()
        0 * connection.createStatement()
        noExceptionThrown()
    }
}
