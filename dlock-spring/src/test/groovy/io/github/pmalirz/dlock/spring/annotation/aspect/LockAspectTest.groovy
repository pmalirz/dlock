package io.github.pmalirz.dlock.spring.annotation.aspect

import io.github.pmalirz.dlock.api.KeyLock
import io.github.pmalirz.dlock.api.LockHandle
import io.github.pmalirz.dlock.spring.annotation.Lock
import io.github.pmalirz.dlock.spring.annotation.LockKeyParam
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import spock.lang.Specification

import java.util.Optional

class LockAspectTest extends Specification {

    KeyLock keyLock = Mock()
    LockAspect lockAspect = new LockAspect(keyLock)
    ProceedingJoinPoint joinPoint = Mock()
    MethodSignature signature = Mock()

    def "should execute method when lock acquired"() {
        given:
        def lockHandle = new LockHandle("handle1")
        keyLock.tryLock("test-key", 10) >> Optional.of(lockHandle)

        def method = TestBean.class.getMethod("lockedMethod")
        signature.getMethod() >> method
        joinPoint.getSignature() >> signature
        joinPoint.getTarget() >> new TestBean()
        joinPoint.proceed() >> "result"
        joinPoint.getArgs() >> []

        when:
        def result = lockAspect.aroundLockedMethod(joinPoint)

        then:
        1 * keyLock.unlock(lockHandle)
        result == "result"
    }

    def "should return null when lock not acquired"() {
        given:
        keyLock.tryLock("test-key", 10) >> Optional.empty()

        def method = TestBean.class.getMethod("lockedMethod")
        signature.getMethod() >> method
        joinPoint.getSignature() >> signature
        joinPoint.getTarget() >> new TestBean()
        joinPoint.getArgs() >> []

        when:
        def result = lockAspect.aroundLockedMethod(joinPoint)

        then:
        0 * joinPoint.proceed()
        result == null
    }

    def "should handle null LockKeyParam"() {
        given:
        def lockHandle = new LockHandle("handle1")
        keyLock.tryLock("test-key-null", 10) >> Optional.of(lockHandle)

        def method = TestBean.class.getMethod("lockedMethodWithParam", String.class)
        signature.getMethod() >> method
        joinPoint.getSignature() >> signature
        joinPoint.getTarget() >> new TestBean()
        joinPoint.proceed() >> "result"
        joinPoint.getArgs() >> [null]

        when:
        def result = lockAspect.aroundLockedMethod(joinPoint)

        then:
        1 * keyLock.unlock(lockHandle)
        result == "result"
    }

    static class TestBean {
        @Lock(key = "test-key", expirationSeconds = 10)
        String lockedMethod() {
            return "result"
        }

        @Lock(key = "test-key-{param}", expirationSeconds = 10)
        String lockedMethodWithParam(@LockKeyParam("param") String param) {
            return "result"
        }
    }
}
