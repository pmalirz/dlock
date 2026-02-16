package com.dlock.jdbc;

import java.util.Arrays;

/**
 * Supported RDBMS.
 *
 * @author Przemyslaw Malirz
 */
public enum DatabaseType {
    H2,
    ORACLE,
    POSTGRESQL;

    public static String[] valuesAsString() {
        return Arrays.stream(values())
                .map(Enum::toString)
                .toArray(String[]::new);
    }
}
