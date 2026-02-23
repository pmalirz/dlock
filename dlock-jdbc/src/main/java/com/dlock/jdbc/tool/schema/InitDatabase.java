package com.dlock.jdbc.tool.schema;

import com.dlock.jdbc.tool.script.ScriptResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Database initiator creates required structures in the database.
 * The execution is safe even if multiple instances try to initialize the database
 * concurrently, as the DDL scripts use "IF NOT EXISTS" (or equivalent) clauses.
 * <p>
 * This class is primarily intended for testing and development environments.
 * Production environments should ideally manage database schemas via migration tools
 * (e.g., Flyway, Liquibase) rather than relying on application startup DDLs.
 *
 * @author Przemyslaw Malirz
 */
public class InitDatabase {

    private final ScriptResolver scriptResolver;
    private final DataSource dataSource;

    public InitDatabase(ScriptResolver scriptResolver, DataSource dataSource) {
        this.scriptResolver = scriptResolver;
        this.dataSource = dataSource;
    }

    public void createDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            if (tableExists(conn)) {
                return;
            }
            List<String> ddls = scriptResolver.resolveDDLScripts();
            for (String ddl : ddls) {
                if (ddl.isBlank())
                    continue;
                try (Statement createStatement = conn.createStatement()) {
                    createStatement.execute(ddl);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private boolean tableExists(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, scriptResolver.getTableName(), null)) {
            return rs.next();
        }
    }
}
