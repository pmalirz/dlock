package com.dlock

import com.dlock.core.SimpleLocalKeyLock
import org.openjdk.jmh.annotations.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Benchmark for {@link LocalKeyLock}.
 *
 * @author Przemyslaw Malirz
 */
open class LocalKeyLockBenchmark {

    @State(Scope.Benchmark)
    open class ExecutionPlan {

        val localKeyLock = SimpleLocalKeyLock

        @Setup(Level.Trial)
        fun setUp() {
            localKeyLock.tryLock("A", 100000)
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun tryAndReleaseLockNoCollision(executionPlan: ExecutionPlan) {
        val lockHandle = executionPlan.localKeyLock.tryLock(UUID.randomUUID().toString(), 1)
        assert(lockHandle.isPresent)
        executionPlan.localKeyLock.unlock(lockHandle.get())
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun tryLockAlwaysCollision(executionPlan: ExecutionPlan) {
        val lockHandle = executionPlan.localKeyLock.tryLock("A", 10000)
        assert(!lockHandle.isPresent)
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    fun tryLockExpiresEverySecond(executionPlan: ExecutionPlan) {
        executionPlan.localKeyLock.tryLock("B", 1)
    }

}