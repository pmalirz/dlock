package com.dlock.spring.annotation.aspect

import com.dlock.core.providers.ClosableKeyLockProvider
import com.dlock.spring.annotation.Lock
import com.dlock.spring.annotation.aspect.utils.LockAspectsUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component


/**
 * The aspect handling the {@link Lock} annotation.
 *
 * @author Przemyslaw Malirz
 */
@Aspect
@Component
class LockAspect @Autowired constructor(private val keyLockProvider: ClosableKeyLockProvider) {

    @Around("@annotation(com.dlock.spring.annotation.Lock)")
    @Throws(Throwable::class, IllegalStateException::class)
    fun aroundLockedMethod(joinPoint: ProceedingJoinPoint) {

        val targetMethod = LockAspectsUtil.getMethod(joinPoint)
                ?: throw IllegalStateException("Couldn't find a method annotated with the @Lock annotation on the pointcut $joinPoint")

        val lockAnnotation = AnnotationUtils.findAnnotation(targetMethod, Lock::class.java)
                ?: throw IllegalStateException("Couldn't find the @Lock annotation on the pointcut $joinPoint")

        var lockKeyValue = lockAnnotation.key

        val parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod)

        parameters.forEach {
            val (paramIndex, paramName) = it
            lockKeyValue = lockKeyValue.replace("{$paramName}", joinPoint.args[paramIndex].toString())
        }
        keyLockProvider.withLock(lockKeyValue, lockAnnotation.expirationSeconds) {
            joinPoint.proceed()
        }
    }

}