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
                    if (isIgnorableError(e)) {
                        // ignore
                    } else {
                        throw new RuntimeException("Failed to initialize database", e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private boolean isIgnorableError(SQLException e) {
        // Oracle: ORA-00955: name is already used by an existing object
        // H2 and PostgreSQL use "IF NOT EXISTS" in their DDL scripts, so they don't throw for existing objects.
        return e.getErrorCode() == 955;
    }
}
