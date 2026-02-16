package com.dlock.core;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;
import com.dlock.core.expiration.LockExpirationPolicy;
import com.dlock.core.handle.LockHandleIdGenerator;
import com.dlock.core.model.ReadLockRecord;
import com.dlock.core.model.WriteLockRecord;
import com.dlock.core.repository.LockRepository;
import com.dlock.core.util.time.DateTimeProvider;

import java.util.Optional;

/**
 * The simplest implementation of the {@link KeyLock} interface based on the
 * repository.
 *
 * @author Przemyslaw Malirz
 */
public class SimpleKeyLock implements KeyLock {

    private final LockRepository lockRepository;
    private final LockHandleIdGenerator lockHandleIdGenerator;
    private final LockExpirationPolicy lockExpirationPolicy;
    private final DateTimeProvider dateTimeProvider;

    public SimpleKeyLock(LockRepository lockRepository,
            LockHandleIdGenerator lockHandleIdGenerator,
            LockExpirationPolicy lockExpirationPolicy,
            DateTimeProvider dateTimeProvider) {
        this.lockRepository = lockRepository;
        this.lockHandleIdGenerator = lockHandleIdGenerator;
        this.lockExpirationPolicy = lockExpirationPolicy;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public Optional<LockHandle> tryLock(String lockKey, long expirationSeconds) {
        ReadLockRecord currentLockRecord = lockRepository.findLockByKey(lockKey);

        if (expiredOrNotExists(currentLockRecord)) {
            if (currentLockRecord != null) {
                breakLock(currentLockRecord);
            }
            return createNewLock(lockKey, expirationSeconds);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void unlock(LockHandle lockHandle) {
        ReadLockRecord lock = lockRepository.findLockByHandleId(lockHandle.handleId());
        if (lock != null) {
            breakLock(lock);
        }
    }

    private Optional<LockHandle> createNewLock(String lockKey, long expirationSeconds) {
        WriteLockRecord newLockRecord = createLockRecord(lockKey, expirationSeconds);
        boolean lockCreated = lockRepository.createLock(newLockRecord);
        if (lockCreated) {
            return Optional.of(new LockHandle(newLockRecord.lockHandleId()));
        } else {
            return Optional.empty();
        }
    }

    private WriteLockRecord createLockRecord(String lockKey, long expirationSeconds) {
        String lockHandleId = lockHandleIdGenerator.generate();
        return new WriteLockRecord(lockKey, lockHandleId, dateTimeProvider.now(), expirationSeconds);
    }

    private boolean expiredOrNotExists(ReadLockRecord currentLock) {
        return currentLock == null || expired(currentLock);
    }

    private boolean expired(ReadLockRecord readLockRecord) {
        return lockExpirationPolicy.expired(readLockRecord);
    }

    private void breakLock(ReadLockRecord readLockRecord) {
        lockRepository.removeLock(readLockRecord.lockHandleId());
    }

}
