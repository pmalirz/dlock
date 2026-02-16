package com.dlock.core.repository;

import com.dlock.core.model.ReadLockRecord;
import com.dlock.core.model.WriteLockRecord;

/**
 * Repository interface for lock persistence.
 *
 * @author Przemyslaw Malirz
 */
public interface LockRepository {

    boolean createLock(WriteLockRecord lockRecord);

    ReadLockRecord findLockByHandleId(String lockHandleId);

    ReadLockRecord findLockByKey(String lockKey);

    void removeLock(String lockHandleId);

}
