package com.dlock.core.repository;

import com.dlock.core.model.ReadLockRecord;
import com.dlock.core.model.WriteLockRecord;
import com.dlock.core.util.time.DateTimeProvider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * This is an in-memory repository implementation backed by the concurrent Set
 * implementation
 * ({@link ConcurrentHashMap#newKeySet}).
 *
 * @author Przemyslaw Malirz
 */
public class LocalLockRepository implements LockRepository {

    private final Set<WriteLockRecord> NAMED_LOCK = ConcurrentHashMap.newKeySet();
    private final DateTimeProvider dateTimeProvider;

    public LocalLockRepository(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public boolean createLock(WriteLockRecord lockRecord) {
        return NAMED_LOCK.add(lockRecord);
    }

    @Override
    public ReadLockRecord findLockByHandleId(String lockHandleId) {
        return findBy(lock -> lock.lockHandleId().equals(lockHandleId));
    }

    @Override
    public ReadLockRecord findLockByKey(String lockKey) {
        return findBy(lock -> lock.lockKey().equals(lockKey));
    }

    @Override
    public void removeLock(String lockHandleId) {
        NAMED_LOCK.removeIf(lock -> lock.lockHandleId().equals(lockHandleId));
    }

    private ReadLockRecord findBy(Predicate<WriteLockRecord> predicate) {
        return NAMED_LOCK.stream()
                .filter(predicate)
                .findFirst()
                .map(it -> ReadLockRecord.of(it, dateTimeProvider.now()))
                .orElse(null);
    }

}
