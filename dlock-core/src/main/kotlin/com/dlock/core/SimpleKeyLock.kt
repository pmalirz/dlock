package com.dlock.core

import com.dlock.api.KeyLock
import com.dlock.api.LockHandle
import com.dlock.core.expiration.LockExpirationPolicy
import com.dlock.core.handle.LockHandleIdGenerator
import com.dlock.core.model.ReadLockRecord
import com.dlock.core.model.WriteLockRecord
import com.dlock.core.repository.LockRepository
import com.dlock.core.util.time.DateTimeProvider
import java.util.*

/**
 * The simplest implementation of the {@link KeyLock} interface based on the repository.
 *
 * @param lockRepository use for persisting lock in a storage
 * @param lockHandleIdGenerator generates globally unique handle identifier
 * @param lockExpirationPolicy verifies whether the given lock expired
 * @param dateTimeProvider used to set the creation time of the given lock
 *
 * @author Przemyslaw Malirz
 */
open class SimpleKeyLock(
        private val lockRepository: LockRepository,
        private val lockHandleIdGenerator: LockHandleIdGenerator,
        private val lockExpirationPolicy: LockExpirationPolicy,
        private val dateTimeProvider: DateTimeProvider)
    : KeyLock {

    override fun tryLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle> {
        val currentLockRecord = lockRepository.findLockByKey(lockKey)

        return if (expiredOrNotExists(currentLockRecord)) {
            currentLockRecord?.breakLock()
            return createNewLock(lockKey, expirationSeconds)
        } else {
            Optional.empty()
        }
    }

    override fun unlock(lockHandle: LockHandle) {
        lockRepository.findLockByHandleId(lockHandle.handleId)?.breakLock()
    }

    private fun createNewLock(lockKey: String, expirationSeconds: Long): Optional<LockHandle> {
        val newLockRecord = createLockRecord(lockKey, expirationSeconds)
        val lockCreated = lockRepository.createLock(newLockRecord)
        return if (lockCreated) {
            Optional.of(LockHandle(newLockRecord.lockHandleId))
        } else {
            Optional.empty()
        }
    }

    private fun createLockRecord(lockKey: String, expirationSeconds: Long): WriteLockRecord {
        val lockHandleId = lockHandleIdGenerator.generate()
        return WriteLockRecord(lockKey, lockHandleId, dateTimeProvider.now(), expirationSeconds)
    }

    private fun expiredOrNotExists(currentLock: ReadLockRecord?): Boolean {
        return currentLock == null || currentLock.expired()
    }

    private fun ReadLockRecord.expired(): Boolean {
        return lockExpirationPolicy.expired(this)
    }

    private fun ReadLockRecord.breakLock() {
        lockRepository.removeLock(this.lockHandleId)
    }

}