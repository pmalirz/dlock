package com.dlock.core.expiration

import com.dlock.core.model.ReadLockRecord

/**
 * Lock expiration policy.
 *
 * @author Przemyslaw Malirz
 */
interface LockExpirationPolicy {

    /**
     * Returns true when the lock should expire.
     *
     * @param readLockRecord
     */
    fun expired(readLockRecord: ReadLockRecord): Boolean

}