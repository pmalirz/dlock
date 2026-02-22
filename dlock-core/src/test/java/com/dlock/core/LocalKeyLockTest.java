package com.dlock.core;

import com.dlock.api.LockHandle;
import com.dlock.core.expiration.LocalLockExpirationPolicy;
import com.dlock.core.handle.LockHandleUUIDIdGenerator;
import com.dlock.core.repository.LocalLockRepository;
import com.dlock.core.util.time.DateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalKeyLockTest {

    private SimpleKeyLock localKeyLock;
    private TestDateTimeProvider dateTimeProvider;

    private static class TestDateTimeProvider implements DateTimeProvider {
        private long timeAdditionSecond = 0L;

        void moveToTheFuture() {
            timeAdditionSecond = 10;
        }

        void backToNow() {
            timeAdditionSecond = 0;
        }

        @Override
        public LocalDateTime now() {
            return LocalDateTime.now().plusSeconds(timeAdditionSecond);
        }
    }

    @BeforeEach
    void setup() {
        dateTimeProvider = new TestDateTimeProvider();
        localKeyLock = new SimpleKeyLock(
                new LocalLockRepository(dateTimeProvider),
                new LockHandleUUIDIdGenerator(),
                new LocalLockExpirationPolicy(),
                dateTimeProvider);
    }

    @Test
    void tryLockAndUnlock() {
        Optional<LockHandle> lock1 = localKeyLock.tryLock("a", 1000);
        Optional<LockHandle> lock2 = localKeyLock.tryLock("a", 1000);

        assertTrue(lock1.isPresent());
        assertFalse(lock2.isPresent());

        localKeyLock.unlock(lock1.get());

        Optional<LockHandle> lock3 = localKeyLock.tryLock("a", 1000);
        assertTrue(lock3.isPresent());
    }

    @Test
    void tryLockWithExpiration() {
        Optional<LockHandle> lock1 = localKeyLock.tryLock("b", 1);
        assertTrue(lock1.isPresent());

        dateTimeProvider.moveToTheFuture(); // let's fast forward to the future

        // can be taken as the previous lock expired
        Optional<LockHandle> lock2 = localKeyLock.tryLock("b", 1000);
        assertTrue(lock2.isPresent());

        dateTimeProvider.backToNow();

        localKeyLock.unlock(lock2.get());

        Optional<LockHandle> lock3 = localKeyLock.tryLock("b", 1000);
        assertTrue(lock3.isPresent());
    }

    @Test
    void testConcurrentLocking() throws InterruptedException {
        int threads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        SimpleKeyLock keyLock = new SimpleKeyLock(
                new LocalLockRepository(DateTimeProvider.SYSTEM),
                new LockHandleUUIDIdGenerator(),
                new LocalLockExpirationPolicy(),
                DateTimeProvider.SYSTEM);

        List<String> acquiredLocks = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    Optional<LockHandle> handle = keyLock.tryLock("concurrent-key", 10);
                    if (handle.isPresent()) {
                        acquiredLocks.add(handle.get().handleId());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // Only one thread should acquire the lock
        assertEquals(1, acquiredLocks.size(), "More than one thread acquired the lock! Acquired: " + acquiredLocks);
    }

}
