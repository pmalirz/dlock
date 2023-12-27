package com.dlock.core.repository

import com.dlock.core.model.WriteLockRecord
import com.dlock.core.model.ReadLockRecord

/**
 * Access database LockRecord.
 *
 * @author Przemyslaw Malirz
 */
interface LockRepository {

    /**
     * Inserts a brand-new lock into the database.
     *
     * @return {@code true} when record created
     */
    fun createLock(lockRecord: WriteLockRecord): Boolean

    /**
     * Finds a lock by its handle ID in the lock repository.
     *
     * @param lockHandleId The handle ID of the lock to find.
     * @return The lock with the specified handle ID if found, otherwise null.
     */
    fun findLockByHandleId(lockHandleId: String): ReadLockRecord?

    /**
     * Finds a lock by its key in the lock repository.
     *
     * @param lockKey The key of the lock to find.
     * @return The lock with the specified key if found, otherwise null.
     */
    fun findLockByKey(lockKey: String): ReadLockRecord?

    /**
     * Deletes a lock from the database by its key.
     * Does nothing when the lock with a given lockHandleId does not exist.
     */
    fun removeLock(lockHandleId: String)

}