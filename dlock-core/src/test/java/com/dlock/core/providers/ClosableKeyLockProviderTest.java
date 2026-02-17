package com.dlock.core.providers;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ClosableKeyLockProviderTest {

    @Test
    void tryLock_LockAcquired() {
        KeyLock keyLock = mock(KeyLock.class);
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.of(new LockHandle("xyz")));

        ClosableKeyLockProvider keyLockProvider = new ClosableKeyLockProvider(keyLock);

        AtomicReference<LockHandle> lockHandleRef = new AtomicReference<>();

        keyLockProvider.tryLock("a", 1, (lockHandle) -> {
            lockHandleRef.set(lockHandle);
            assertEquals("xyz", lockHandle.handleId());
        });

        verify(keyLock).tryLock("a", 1);
        verify(keyLock).unlock(lockHandleRef.get());
        verifyNoMoreInteractions(keyLock);
    }

    @Test
    void tryLock_LockNotAcquired() {
        KeyLock keyLock = mock(KeyLock.class);
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.empty());

        ClosableKeyLockProvider keyLockProvider = new ClosableKeyLockProvider(keyLock);

        keyLockProvider.tryLock("a", 1, (lockHandle) -> {
            fail("Lock should not be taken");
        });

        verify(keyLock).tryLock("a", 1);
        verifyNoMoreInteractions(keyLock);
    }

}
