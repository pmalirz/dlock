package io.github.pmalirz.dlock;

import io.github.pmalirz.dlock.core.SimpleLocalKeyLock;
import io.github.pmalirz.dlock.api.LockHandle;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for {@link SimpleLocalKeyLock}.
 *
 * @author Przemyslaw Malirz
 */
public class LocalKeyLockBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        final SimpleLocalKeyLock localKeyLock = new SimpleLocalKeyLock();

        @Setup(Level.Trial)
        public void setUp() {
            localKeyLock.tryLock("A", 100000);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void tryAndReleaseLockNoCollision(ExecutionPlan executionPlan, Blackhole bh) {
        Optional<LockHandle> lockHandle = executionPlan.localKeyLock.tryLock(UUID.randomUUID().toString(), 1);
        bh.consume(lockHandle);
        if (lockHandle.isPresent()) {
            executionPlan.localKeyLock.unlock(lockHandle.get());
        } else {
            throw new AssertionError("Lock acquisition failed");
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void tryLockAlwaysCollision(ExecutionPlan executionPlan, Blackhole bh) {
        Optional<LockHandle> lockHandle = executionPlan.localKeyLock.tryLock("A", 10000);
        bh.consume(lockHandle);
        if (lockHandle.isPresent()) {
            throw new AssertionError("Should not be able to acquire lock");
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void tryLockExpiresEverySecond(ExecutionPlan executionPlan, Blackhole bh) {
        bh.consume(executionPlan.localKeyLock.tryLock("B", 1));
    }
}
