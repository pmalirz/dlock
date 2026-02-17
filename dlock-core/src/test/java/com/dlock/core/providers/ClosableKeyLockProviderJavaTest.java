package com.dlock.core.providers;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

/**
 * Tests of {@link ClosableKeyLockProviderJavaTest}. Java version to see how
 * try-with-resources work.
 *
 * @author Przemyslaw Malirz
 */
class ClosableKeyLockProviderJavaTest {

    @Test
    void tryLock_LockAcquired() {
        final KeyLock keyLock = mock(KeyLock.class);
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.of(new LockHandle("xyz")));

        final ClosableKeyLockProvider keyLockProvider = new ClosableKeyLockProvider(keyLock);

        final AtomicReference<LockHandle> lockHandle = new AtomicReference<>();

        keyLockProvider.tryLock("a", 1, lockHandle::set);

        verify(keyLock).tryLock("a", 1);
        verify(keyLock).unlock(lockHandle.get());
        verifyNoMoreInteractions(keyLock);
    }

    @Test
    void tryLock_LockNotAcquired() {
        final KeyLock keyLock = mock(KeyLock.class);
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.empty());

        final ClosableKeyLockProvider keyLockProvider = new ClosableKeyLockProvider(keyLock);
        final AtomicReference<LockHandle> lockHandle = new AtomicReference<>();
        keyLockProvider.tryLock("a", 1, lockHandle::set);

        Assertions.assertNull(lockHandle.get());

        verify(keyLock).tryLock("a", 1);
        verifyNoMoreInteractions(keyLock);
    }

}
