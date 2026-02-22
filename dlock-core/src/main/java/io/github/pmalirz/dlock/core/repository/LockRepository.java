package io.github.pmalirz.dlock.core.repository;

import io.github.pmalirz.dlock.core.model.ReadLockRecord;
import io.github.pmalirz.dlock.core.model.WriteLockRecord;
import io.github.pmalirz.dlock.api.exception.LockException;

/**
 * Repository interface for lock persistence.
 *
 * @author Przemyslaw Malirz
 */
public interface LockRepository {

    /**
     * Creates a new lock record.
     * @param lockRecord the lock record to create
     * @return true if the lock was created, false otherwise (e.g. if lock already exists)
     * @throws LockException if an unexpected error occurs
     */
    boolean createLock(WriteLockRecord lockRecord);

    /**
     * Finds a lock by its handle ID.
     * @param lockHandleId the handle ID
     * @return the lock record, or null if not found
     * @throws LockException if an unexpected error occurs
     */
    ReadLockRecord findLockByHandleId(String lockHandleId);

    /**
     * Finds a lock by its key.
     * @param lockKey the lock key
     * @return the lock record, or null if not found
     * @throws LockException if an unexpected error occurs
     */
    ReadLockRecord findLockByKey(String lockKey);

    /**
     * Removes a lock by its handle ID.
     * @param lockHandleId the handle ID
     * @throws LockException if an unexpected error occurs
     */
    void removeLock(String lockHandleId);

}
