package io.github.pmalirz.dlock.jdbc.repository;

import io.github.pmalirz.dlock.api.exception.LockException;
import io.github.pmalirz.dlock.core.model.ReadLockRecord;
import io.github.pmalirz.dlock.core.model.WriteLockRecord;
import io.github.pmalirz.dlock.core.repository.LockRepository;
import io.github.pmalirz.dlock.jdbc.tool.script.ScriptResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * JDBC access to the lock storage. Lock is represented by the
 * {@link WriteLockRecord} class.
 *
 * @author Przemyslaw Malirz
 */
public class JDBCLockRepository implements LockRepository {

    private final DataSource dataSource;
    private final String insertSQL;
    private final String findByHandleSQL;
    private final String findByKeySQL;
    private final String removeByHandleSQL;
    private final int insertParamsCount;

    public JDBCLockRepository(ScriptResolver scriptResolver, DataSource dataSource) {
        this.dataSource = dataSource;
        this.insertSQL = scriptResolver.resolveScript("lock.insert");
        this.findByHandleSQL = scriptResolver.resolveScript("lock.findByHandle");
        this.findByKeySQL = scriptResolver.resolveScript("lock.findByKey");
        this.removeByHandleSQL = scriptResolver.resolveScript("lock.removeByHandle");
        this.insertParamsCount = countOccurrences(insertSQL, '?');
    }

    @Override
    public boolean createLock(WriteLockRecord lockRecord) {
        try (Connection connection = dataSource.getConnection()) {
            boolean recordCreated = executeInsert(connection, lockRecord);
            if (recordCreated) {
                commit(connection);
            }
            return recordCreated;
        } catch (SQLException e) {
            // SQLState class 23 = integrity constraint violation (unique key, etc.)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return false;
            }
            throw new LockException("Error while creating lock: " + lockRecord, e);
        }
    }

    @Override
    public ReadLockRecord findLockByHandleId(String lockHandleId) {
        try (Connection connection = dataSource.getConnection()) {
            return executeFindByHandleId(connection, lockHandleId);
        } catch (SQLException e) {
            throw new LockException("Error while finding lock by handle: " + lockHandleId, e);
        }
    }

    @Override
    public ReadLockRecord findLockByKey(String lockKey) {
        try (Connection connection = dataSource.getConnection()) {
            return executeFindByKey(connection, lockKey);
        } catch (SQLException e) {
            throw new LockException("Error while finding lock by key: " + lockKey, e);
        }
    }

    @Override
    public void removeLock(String lockHandleId) {
        try (Connection connection = dataSource.getConnection()) {
            boolean recordRemoved = executeRemove(connection, lockHandleId);
            if (recordRemoved) {
                commit(connection);
            }
        } catch (SQLException e) {
            throw new LockException("Error while removing lock: " + lockHandleId, e);
        }
    }

    // SQL / JDBC
    // -----------------------------------------------------------------------------------------------

    /** Select SQL PreparedStatement. */
    private ReadLockRecord executeFindByHandleId(Connection connection, String lockHandleId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(findByHandleSQL)) {
            ps.setString(1, lockHandleId);

            boolean executeResult = ps.execute();
            ResultSet resultSet = ps.getResultSet();

            if (!executeResult || !resultSet.next()) {
                return null;
            }

            String lockKey = resultSet.getString(1);
            Timestamp lockCreatedTime = resultSet.getTimestamp(3);
            long lockExpirationSeconds = resultSet.getLong(4);
            Timestamp currentTime = resultSet.getTimestamp(5);
            return new ReadLockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds,
                    currentTime.toLocalDateTime());
        }
    }

    /** Select SQL PreparedStatement. */
    private ReadLockRecord executeFindByKey(Connection connection, String lockKey) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(findByKeySQL)) {
            ps.setString(1, lockKey);

            boolean executeResult = ps.execute();
            ResultSet resultSet = ps.getResultSet();

            if (!executeResult || !resultSet.next()) {
                return null;
            }

            String lockHandleId = resultSet.getString(2);
            Timestamp lockCreatedTime = resultSet.getTimestamp(3);
            long lockExpirationSeconds = resultSet.getLong(4);
            Timestamp currentTime = resultSet.getTimestamp(5);
            return new ReadLockRecord(lockKey, lockHandleId, lockCreatedTime.toLocalDateTime(), lockExpirationSeconds,
                    currentTime.toLocalDateTime());
        }
    }

    /** Insert SQL PreparedStatement. */
    private boolean executeInsert(Connection connection, WriteLockRecord lockRecord) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(insertSQL)) {
            ps.setString(1, lockRecord.lockKey());
            ps.setString(2, lockRecord.lockHandleId());

            ps.setLong(3, lockRecord.expirationSeconds());

            if (insertParamsCount == 4) {
                ps.setString(4, lockRecord.lockKey());
            }

            return ps.executeUpdate() == 1;
        }
    }

    private int countOccurrences(String str, char ch) {
        return (int) str.chars().filter(c -> c == ch).count();
    }

    /** Delete SQL PreparedStatement. */
    private boolean executeRemove(Connection connection, String lockHandleId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(removeByHandleSQL)) {
            ps.setString(1, lockHandleId);
            return ps.executeUpdate() == 1;
        }
    }

    private void commit(Connection connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
    }

}
