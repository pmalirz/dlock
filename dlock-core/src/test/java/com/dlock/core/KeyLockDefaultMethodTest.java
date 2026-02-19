package com.dlock.core;

import com.dlock.api.KeyLock;
import com.dlock.api.LockHandle;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@link KeyLock} default methods.
 */
class KeyLockDefaultMethodTest {

    static class TestKeyLock implements KeyLock {
        private final LockHandle handleToReturn;
        public boolean unlocked = false;
        public int tryLockCalls = 0;

        public TestKeyLock(LockHandle handleToReturn) {
            this.handleToReturn = handleToReturn;
        }

        @Override
        public Optional<LockHandle> tryLock(String lockKey, long expirationSeconds) {
            tryLockCalls++;
            return Optional.ofNullable(handleToReturn);
        }

        @Override
        public void unlock(LockHandle lockHandle) {
            if (handleToReturn != null && handleToReturn.equals(lockHandle)) {
                unlocked = true;
            }
        }
    }

    @Test
    void tryLockConsumer_LockAcquired() {
        LockHandle handle = new LockHandle("h1");
        TestKeyLock keyLock = new TestKeyLock(handle);
        AtomicBoolean executed = new AtomicBoolean(false);

        keyLock.tryLock("key", 10, h -> {
            assertEquals(handle, h);
            executed.set(true);
        });

        assertTrue(executed.get());
        assertTrue(keyLock.unlocked);
        assertEquals(1, keyLock.tryLockCalls);
    }

    @Test
    void tryLockConsumer_LockNotAcquired() {
        TestKeyLock keyLock = new TestKeyLock(null);
        AtomicBoolean executed = new AtomicBoolean(false);

        keyLock.tryLock("key", 10, h -> {
            executed.set(true);
        });

        assertFalse(executed.get());
        assertFalse(keyLock.unlocked);
        assertEquals(1, keyLock.tryLockCalls);
    }

    @Test
    void tryLockFunction_LockAcquired() {
        LockHandle handle = new LockHandle("h1");
        TestKeyLock keyLock = new TestKeyLock(handle);

        Optional<String> result = keyLock.tryLock("key", 10, h -> {
            assertEquals(handle, h);
            return "result";
        });

        assertTrue(result.isPresent());
        assertEquals("result", result.get());
        assertTrue(keyLock.unlocked);
        assertEquals(1, keyLock.tryLockCalls);
    }

    @Test
    void tryLockFunction_LockNotAcquired() {
        TestKeyLock keyLock = new TestKeyLock(null);

        Optional<String> result = keyLock.tryLock("key", 10, (h) -> {
            return "result";
        });

        assertFalse(result.isPresent());
        assertFalse(keyLock.unlocked);
        assertEquals(1, keyLock.tryLockCalls);
    }

    @Test
    void tryLockFunction_ExceptionInAction() {
        LockHandle handle = new LockHandle("h1");
        TestKeyLock keyLock = new TestKeyLock(handle);

        try {
            keyLock.tryLock("key", 10, h -> {
                if (true)
                    throw new RuntimeException("fail");
            });
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("fail", e.getMessage());
        }

        assertTrue(keyLock.unlocked); // Should still unlock
    }
}
