package io.github.pmalirz.dlock.core.expiration;

import io.github.pmalirz.dlock.core.model.ReadLockRecord;

/**
 * Verifies whether the given lock expired.
 *
 * @author Przemyslaw Malirz
 */
public interface LockExpirationPolicy {

    boolean expired(ReadLockRecord readLockRecord);

}
