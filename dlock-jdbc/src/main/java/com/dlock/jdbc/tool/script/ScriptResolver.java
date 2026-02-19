package com.dlock.jdbc.tool.script;

import com.dlock.jdbc.DatabaseType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Loads database script for a given database and scrip type.
 *
 * @author Przemyslaw Malirz
 */
public class ScriptResolver {

    private final DatabaseType databaseType;
    private final String tableName;
    private final String tableNamePlaceholder = "@@tableName@@";
    private final Properties sqlResource = new Properties();

    public ScriptResolver(DatabaseType databaseType, String tableName) {
        this.databaseType = databaseType;
        this.tableName = tableName;
        init();
    }

    private void init() {
        try (InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("db/" + databaseType + "-sql.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("db/" + databaseType + "-sql.properties not found");
            }
            String rawContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            String fileContent = rawContent.replace(tableNamePlaceholder, tableName);
            sqlResource.load(new StringReader(fileContent));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL properties", e);
        }
    }

    public String resolveScript(String scriptPropertyKey) {
        return sqlResource.getProperty(scriptPropertyKey);
    }

    public List<String> resolveDDLScripts() {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/db/" + databaseType + "-create.sql")) {
            if (inputStream == null) {
                return Collections.emptyList();
            }
            String initScriptTemplate = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            String entireScriptContent = initScriptTemplate.replace(tableNamePlaceholder, tableName);
            return List.of(entireScriptContent.split(";"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DDL scripts", e);
        }
    }

    public String getTableName() {
        return tableName;
    }
}
