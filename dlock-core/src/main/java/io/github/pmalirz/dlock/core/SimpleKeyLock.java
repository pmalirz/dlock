package io.github.pmalirz.dlock.core;

import io.github.pmalirz.dlock.api.KeyLock;
import io.github.pmalirz.dlock.api.LockHandle;
import io.github.pmalirz.dlock.core.expiration.LockExpirationPolicy;
import io.github.pmalirz.dlock.core.handle.LockHandleIdGenerator;
import io.github.pmalirz.dlock.core.model.ReadLockRecord;
import io.github.pmalirz.dlock.core.model.WriteLockRecord;
import io.github.pmalirz.dlock.core.repository.LockRepository;
import io.github.pmalirz.dlock.core.util.time.DateTimeProvider;

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
        if (lockKey == null || lockKey.isBlank()) {
            throw new IllegalArgumentException("lockKey must not be null or blank");
        }
        if (lockKey.length() > 1000) {
            throw new IllegalArgumentException("lockKey must not be longer than 1000 characters");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("expirationSeconds must be greater than 0");
        }

        // Optimistic approach: try to create a new lock first
        Optional<LockHandle> newLock = createNewLock(lockKey, expirationSeconds);
        if (newLock.isPresent()) {
            return newLock;
        }

        // If failed, check if the existing lock is expired
        ReadLockRecord currentLockRecord = lockRepository.findLockByKey(lockKey);

        if (currentLockRecord != null && expired(currentLockRecord)) {
            breakLock(currentLockRecord);
            return createNewLock(lockKey, expirationSeconds);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void unlock(LockHandle lockHandle) {
        lockRepository.removeLock(lockHandle.handleId());
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

    private boolean expired(ReadLockRecord readLockRecord) {
        return lockExpirationPolicy.expired(readLockRecord);
    }

    private void breakLock(ReadLockRecord readLockRecord) {
        lockRepository.removeLock(readLockRecord.lockHandleId());
    }

}
