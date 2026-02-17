package com.dlock.core;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the
 * {@link KeyLock#tryLock(String, long, java.util.function.Consumer)} default
 * method.
 */
class KeyLockDefaultMethodTest {

    @Test
    void tryLock_LockAcquired_executesActionAndUnlocks() {
        KeyLock keyLock = mock(KeyLock.class, withSettings().defaultAnswer(invocation -> {
            if (invocation.getMethod().isDefault()) {
                return invocation.callRealMethod();
            }
            return null;
        }));
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.of(new LockHandle("xyz")));

        AtomicReference<LockHandle> lockHandleRef = new AtomicReference<>();

        keyLock.tryLock("a", 1, (lockHandle) -> {
            lockHandleRef.set(lockHandle);
            assertEquals("xyz", lockHandle.handleId());
        });

        verify(keyLock).tryLock("a", 1);
        verify(keyLock).unlock(lockHandleRef.get());
    }

    @Test
    void tryLock_LockNotAcquired_doesNotExecuteAction() {
        KeyLock keyLock = mock(KeyLock.class, withSettings().defaultAnswer(invocation -> {
            if (invocation.getMethod().isDefault()) {
                return invocation.callRealMethod();
            }
            return null;
        }));
        when(keyLock.tryLock("a", 1)).thenReturn(Optional.empty());

        AtomicReference<LockHandle> lockHandleRef = new AtomicReference<>();

        keyLock.tryLock("a", 1, (lockHandle) -> {
            fail("Action should not be executed when lock is not acquired");
        });

        assertNull(lockHandleRef.get());

        verify(keyLock).tryLock("a", 1);
    }

}
