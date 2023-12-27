package com.dlock.core.repository

import com.dlock.core.model.WriteLockRecord
import com.dlock.core.model.ReadLockRecord
import com.dlock.core.util.time.DateTimeProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * This is an in-memory repository implementation backed by the concurrent Set implementation
 * ({@link ConcurrentHashMap#newKeySet}).
 *
 * @author Przemyslaw Malirz
 */
class LocalLockRepository(
        private val dateTimeProvider: DateTimeProvider
) : LockRepository {

    private val NAMED_LOCK = ConcurrentHashMap.newKeySet<WriteLockRecord>()

    override fun createLock(lockRecord: WriteLockRecord): Boolean {
        return NAMED_LOCK.add(lockRecord)
    }

    override fun findLockByHandleId(lockHandleId: String) = findBy { lock -> lock.lockHandleId == lockHandleId }

    override fun findLockByKey(lockKey: String) = findBy { lock -> lock.lockKey == lockKey }


    override fun removeLock(lockHandleId: String) {
        NAMED_LOCK.removeIf { lock -> lock.lockHandleId == lockHandleId }
    }

    private fun findBy(predicate: (WriteLockRecord) -> Boolean) =
            NAMED_LOCK.find(predicate)?.let {
                ReadLockRecord.of(it, dateTimeProvider.now())
            }

}
