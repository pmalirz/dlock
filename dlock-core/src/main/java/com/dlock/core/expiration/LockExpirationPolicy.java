package com.dlock.core.expiration;

import com.dlock.core.model.ReadLockRecord;

/**
 * Verifies whether the given lock expired.
 *
 * @author Przemyslaw Malirz
 */
public interface LockExpirationPolicy {

    boolean expired(ReadLockRecord readLockRecord);

}
