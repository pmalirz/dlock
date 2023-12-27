package com.dlock.core

import com.dlock.core.expiration.LocalLockExpirationPolicy
import com.dlock.core.handle.LockHandleUUIDIdGenerator
import com.dlock.core.repository.LocalLockRepository
import com.dlock.core.util.time.DateTimeProvider

/**
 * Local, memory-based key lock (uses LocalLockRepository).
 *
 * @author Przemyslaw Malirz
 */
object SimpleLocalKeyLock: SimpleKeyLock(
        LocalLockRepository(DateTimeProvider),
        LockHandleUUIDIdGenerator(),
        LocalLockExpirationPolicy(),
        DateTimeProvider)