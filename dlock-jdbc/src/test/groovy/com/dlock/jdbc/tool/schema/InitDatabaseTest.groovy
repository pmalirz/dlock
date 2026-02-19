package com.dlock.jdbc.tool.schema

import com.dlock.jdbc.tool.script.ScriptResolver
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

class InitDatabaseTest extends Specification {

    ScriptResolver scriptResolver = Mock()
    DataSource dataSource = Mock()
    Connection connection = Mock()
    DatabaseMetaData metaData = Mock()
    Statement statement = Mock()
    ResultSet resultSet = Mock()

    def "should create database when table does not exist"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        scriptResolver.getTableName() >> "DLCK"
        dataSource.getConnection() >> connection
        connection.getMetaData() >> metaData
        metaData.getTables(null, null, _, null) >> resultSet
        resultSet.next() >> false // Table does not exist
        connection.createStatement() >> statement

        when:
        initDatabase.createDatabase()

        then:
        1 * statement.execute("CREATE TABLE DLCK ...")
    }

    def "should not create database when table exists"() {
        given:
        def initDatabase = new InitDatabase(scriptResolver, dataSource)
        scriptResolver.resolveDDLScripts() >> ["CREATE TABLE DLCK ..."]
        scriptResolver.getTableName() >> "DLCK"
        dataSource.getConnection() >> connection
        connection.getMetaData() >> metaData
        metaData.getTables(null, null, "DLCK", null) >> resultSet
        resultSet.next() >> true // Table exists

        when:
        initDatabase.createDatabase()

        then:
        0 * connection.createStatement()
        0 * statement.execute(_)
    }
}
