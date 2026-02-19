package com.dlock.jdbc.tool.schema;

import com.dlock.jdbc.tool.script.ScriptResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Database initiator creates required structures in the database.
 * It can be used concurrently so we have to make sure it works properly when
 * used a few times.
 * Anyway, mostly used for testing as production (and pre-production) regions
 * should not rely on
 * automatic DDL run at start.
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

    public synchronized void createDatabase() {
        List<String> ddls = scriptResolver.resolveDDLScripts();
        String tableName = scriptResolver.getTableName();

        try (Connection conn = dataSource.getConnection()) {
            if (tableExists(conn, tableName)) {
                return;
            }
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

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        return conn.getMetaData().getTables(null, null, tableName, null).next() ||
                conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null).next() ||
                conn.getMetaData().getTables(null, null, tableName.toLowerCase(), null).next();
    }
}
