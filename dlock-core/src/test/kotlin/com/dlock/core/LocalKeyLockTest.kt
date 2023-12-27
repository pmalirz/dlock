package com.dlock.core

import com.dlock.core.expiration.LocalLockExpirationPolicy
import com.dlock.core.handle.LockHandleUUIDIdGenerator
import com.dlock.core.repository.LocalLockRepository
import com.dlock.core.util.time.DateTimeProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

/**
 * Tests of {@link LocalKeyLock}.
 * One can use LocalLockRepository to implement in-memory, single node version of KeyLock.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LocalKeyLockTest {

    private lateinit var localKeyLock: SimpleKeyLock

    private val dateTimeProvider = object : DateTimeProvider {
        private var timeAdditionSecond = 0L

        fun moveToTheFuture() {
            timeAdditionSecond = 10
        }

        fun backToNow() {
            timeAdditionSecond = 0
        }

        override fun now(): LocalDateTime {
            return LocalDateTime.now().plusSeconds(timeAdditionSecond)
        }
    }

    @BeforeAll
    internal fun setup() {


        localKeyLock = SimpleKeyLock(
            LocalLockRepository(dateTimeProvider),
            LockHandleUUIDIdGenerator(),
            LocalLockExpirationPolicy(),
            dateTimeProvider
        )
    }

    @Test
    fun tryLockAndUnlock() {
        val lock1 = localKeyLock.tryLock("a", 1000)
        val lock2 = localKeyLock.tryLock("a", 1000)

        assertTrue(lock1.isPresent)
        assertFalse(lock2.isPresent)

        localKeyLock.unlock(lock1.get())

        val lock3 = localKeyLock.tryLock("a", 1000)
        assertTrue(lock3.isPresent)
    }

    @Test
    fun tryLockWithExpiration() {
        val lock1 = localKeyLock.tryLock("b", 1)
        assertTrue(lock1.isPresent)

        dateTimeProvider.moveToTheFuture() // let's fast forward to the future

        // can be taken as the previous lock expired
        val lock2 = localKeyLock.tryLock("b", 1000)
        assertTrue(lock2.isPresent)

        dateTimeProvider.backToNow()

        localKeyLock.unlock(lock2.get())

        val lock3 = localKeyLock.tryLock("b", 1000)
        assertTrue(lock3.isPresent)
    }

}