package com.dlock.core.repository;

import com.dlock.core.model.ReadLockRecord;
import com.dlock.core.model.WriteLockRecord;
import com.dlock.core.util.time.DateTimeProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is an in-memory repository implementation backed by the concurrent Map
 * implementation
 * ({@link ConcurrentHashMap}).
 *
 * @author Przemyslaw Malirz
 */
public class LocalLockRepository implements LockRepository {

    private final ConcurrentMap<String, WriteLockRecord> NAMED_LOCK = new ConcurrentHashMap<>();
    private final DateTimeProvider dateTimeProvider;

    public LocalLockRepository(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public boolean createLock(WriteLockRecord lockRecord) {
        return NAMED_LOCK.putIfAbsent(lockRecord.lockKey(), lockRecord) == null;
    }

    @Override
    public ReadLockRecord findLockByHandleId(String lockHandleId) {
        return NAMED_LOCK.values().stream()
                .filter(lock -> lock.lockHandleId().equals(lockHandleId))
                .findFirst()
                .map(this::toReadLockRecord)
                .orElse(null);
    }

    @Override
    public ReadLockRecord findLockByKey(String lockKey) {
        WriteLockRecord lockRecord = NAMED_LOCK.get(lockKey);
        if (lockRecord != null) {
            return toReadLockRecord(lockRecord);
        }
        return null;
    }

    @Override
    public void removeLock(String lockHandleId) {
        NAMED_LOCK.values().removeIf(lock -> lock.lockHandleId().equals(lockHandleId));
    }

    private ReadLockRecord toReadLockRecord(WriteLockRecord writeLockRecord) {
        return ReadLockRecord.of(writeLockRecord, dateTimeProvider.now());
    }

}
