package io.github.pmalirz.dlock.jdbc

/**
 * Unit test runtime parameters.
 *
 * @author Przemyslaw Malirz
 */
class TestRuntimeProperties {

    private DatabaseType databaseType
    private String databaseURL
    private String databaseUser
    private String databasePassword
    private boolean databaseInit

    TestRuntimeProperties(DatabaseType databaseType, String databaseURL, String databaseUser, String databasePassword, boolean databaseInit) {
        this.databaseType = databaseType
        this.databaseURL = databaseURL
        this.databaseUser = databaseUser
        this.databasePassword = databasePassword
        this.databaseInit = databaseInit
    }

    /**
     * Loads runtime parameters. Defaults are set if not parameters found (H2 is declared for the database properties).
     * @return an instance of TestRuntimeProperties created based on the runtime parameters
     */
    static TestRuntimeProperties getProperties() {
        def dbType = System.getProperty("dbType", "H2")
        def dbURL = System.getProperty("dbURL", "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1")
        def dbUser = System.getProperty("dbUser", "sa")
        def dbPassword = System.getProperty("dbPassword")
        def dbInit = System.getProperty("dbInit", "true")

        return new TestRuntimeProperties(DatabaseType.valueOf(dbType), dbURL, dbUser, dbPassword, Boolean.valueOf(dbInit))
    }

    DatabaseType getDatabaseType() {
        return databaseType
    }

    String getDatabaseURL() {
        return databaseURL
    }

    String getDatabaseUser() {
        return databaseUser
    }

    String getDatabasePassword() {
        return databasePassword
    }

    /**
     * Should unit test database be created at start? Database user requires specific database permissions (e.g. create table).
     * @return true if database structures should be created
     */
    boolean getDatabaseInit() {
        return databaseInit
    }
}
