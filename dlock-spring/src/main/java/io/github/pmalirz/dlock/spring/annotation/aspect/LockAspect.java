package io.github.pmalirz.dlock.spring.annotation.aspect;

import io.github.pmalirz.dlock.api.KeyLock;
import io.github.pmalirz.dlock.api.LockHandle;
import io.github.pmalirz.dlock.spring.annotation.Lock;
import io.github.pmalirz.dlock.spring.annotation.aspect.utils.LockAspectsUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * The aspect handling the {@link Lock} annotation.
 *
 * @author Przemyslaw Malirz
 */
@Aspect
@Component
public class LockAspect {

    private final KeyLock keyLock;

    @Autowired
    public LockAspect(KeyLock keyLock) {
        this.keyLock = keyLock;
    }

    /**
     * Intercepts method execution to acquire a lock before proceeding.
     * <p>
     * If the lock is acquired, the method body is executed, and the lock is released afterward (in a finally block).
     * If the lock cannot be acquired (e.g., already held by another process), the method execution is skipped,
     * and {@code null} is returned.
     *
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution if the lock is acquired, or {@code null} if the lock is not acquired
     * @throws Throwable if the method execution throws an exception
     * @throws IllegalStateException if the {@code @Lock} annotation is missing (should not happen due to pointcut)
     */
    @Around("@annotation(io.github.pmalirz.dlock.spring.annotation.Lock)")
    public Object aroundLockedMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        Method targetMethod = LockAspectsUtil.getMethod(joinPoint);
        if (targetMethod == null) {
            throw new IllegalStateException(
                    "Couldn't find a method annotated with the @Lock annotation on the pointcut " + joinPoint);
        }

        Lock lockAnnotation = AnnotationUtils.findAnnotation(targetMethod, Lock.class);
        if (lockAnnotation == null) {
            throw new IllegalStateException("Couldn't find the @Lock annotation on the pointcut " + joinPoint);
        }

        String lockKeyValue = lockAnnotation.key();

        List<LockAspectsUtil.LockKeyParameter> parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod);

        lockKeyValue = replaceKeyParameters(lockKeyValue, joinPoint, parameters);

        Optional<LockHandle> lock = keyLock.tryLock(lockKeyValue, lockAnnotation.expirationSeconds());
        if (lock.isPresent()) {
            try {
                return joinPoint.proceed();
            } finally {
                keyLock.unlock(lock.get());
            }
        } else {
            if (targetMethod.getReturnType() == Optional.class) {
                return Optional.empty();
            }
            return null;
        }
    }

    private String replaceKeyParameters(String lockKeyValue, ProceedingJoinPoint joinPoint, List<LockAspectsUtil.LockKeyParameter> parameters) {
        String result = lockKeyValue;
        for (LockAspectsUtil.LockKeyParameter parameter : parameters) {
            Object arg = joinPoint.getArgs()[parameter.index()];
            String argValue = String.valueOf(arg);
            result = result.replace("{" + parameter.name() + "}", argValue);
        }
        return result;
    }

}
