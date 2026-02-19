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

        try (Connection conn = dataSource.getConnection()) {
            for (String ddl : ddls) {
                if (ddl.isBlank())
                    continue;
                try (Statement createStatement = conn.createStatement()) {
                    createStatement.execute(ddl);
                } catch (SQLException e) {
                    // It is possible that the object already exists (e.g. table or index).
                    // In such case we just ignore the error and move forward.
                    // We can't use IF NOT EXISTS for all databases (e.g. Oracle).
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
