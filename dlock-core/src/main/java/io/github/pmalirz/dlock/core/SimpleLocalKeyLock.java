package io.github.pmalirz.dlock.core;

import io.github.pmalirz.dlock.core.expiration.LocalLockExpirationPolicy;
import io.github.pmalirz.dlock.core.handle.LockHandleUUIDIdGenerator;
import io.github.pmalirz.dlock.core.repository.LocalLockRepository;
import io.github.pmalirz.dlock.core.util.time.DateTimeProvider;

/**
 * Local, memory-based key lock (uses LocalLockRepository).
 *
 * @author Przemyslaw Malirz
 */
public class SimpleLocalKeyLock extends SimpleKeyLock {

    public SimpleLocalKeyLock() {
        super(new LocalLockRepository(DateTimeProvider.SYSTEM),
                new LockHandleUUIDIdGenerator(),
                new LocalLockExpirationPolicy(),
                DateTimeProvider.SYSTEM);
    }
}
