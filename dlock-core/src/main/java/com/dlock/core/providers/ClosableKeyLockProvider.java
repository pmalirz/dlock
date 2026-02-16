package com.dlock.core.providers;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Auto-closable {@link KeyLock} provider.
 *
 * @author Przemyslaw Malirz
 */
public class ClosableKeyLockProvider {

    private final KeyLock keyLock;

    public ClosableKeyLockProvider(KeyLock keyLock) {
        this.keyLock = keyLock;
    }

    public void withLock(String lockKey, long expirationSeconds, Consumer<LockHandle> f) {
        Optional<LockHandle> lock = keyLock.tryLock(lockKey, expirationSeconds);
        if (lock.isPresent()) {
            try (ClosableLockHandle closable = new ClosableLockHandle(lock.get())) {
                f.accept(closable.lockHandle);
            }
        }
    }

    /**
     * Auto-closable {@link LockHandle}.
     */
    private class ClosableLockHandle implements AutoCloseable {
        final LockHandle lockHandle;

        ClosableLockHandle(LockHandle lockHandle) {
            this.lockHandle = lockHandle;
        }

        @Override
        public void close() {
            keyLock.unlock(lockHandle);
        }
    }

}
