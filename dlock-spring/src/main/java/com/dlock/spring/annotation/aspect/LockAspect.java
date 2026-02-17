package com.dlock.spring.annotation.aspect;

import com.dlock.api.KeyLock;
import com.dlock.spring.annotation.Lock;
import com.dlock.spring.annotation.aspect.utils.LockAspectsUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

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

    @Around("@annotation(com.dlock.spring.annotation.Lock)")
    public void aroundLockedMethod(ProceedingJoinPoint joinPoint) throws Throwable {

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

        for (LockAspectsUtil.LockKeyParameter parameter : parameters) {
            lockKeyValue = lockKeyValue.replace("{" + parameter.name() + "}",
                    joinPoint.getArgs()[parameter.index()].toString());
        }

        keyLock.tryLock(lockKeyValue, lockAnnotation.expirationSeconds(), (handle) -> {
            try {
                joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
