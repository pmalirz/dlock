package io.github.pmalirz.dlock.spring.annotation.aspect.utils;

import io.github.pmalirz.dlock.spring.annotation.LockKeyParam;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Helps to work with Lock annotations.
 *
 * @author Przemyslaw Malirz
 */
public class LockAspectsUtil {

    private LockAspectsUtil() {
    }

    /**
     * Gets an actual / target method. When the join-point method points to the
     * interface then the method of
     * the actual object / bean is returned.
     *
     * @return target method of an object (bean)
     */
    public static Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.getDeclaringClass().isInterface()) {
            try {
                return joinPoint.getTarget().getClass().getMethod(joinPoint.getSignature().getName(),
                        method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return method;
            }
        } else {
            return method;
        }
    }

    public record LockKeyParameter(int index, String name) {
    }

    /**
     * Returns all the method parameters annotated with the @LockKeyParam
     * annotation.
     *
     * @return List of parameters info
     */
    public static List<LockKeyParameter> getLockKeyMethodParameters(Method method) {
        List<LockKeyParameter> result = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            LockKeyParam annotation = parameter.getAnnotation(LockKeyParam.class);
            if (annotation != null) {
                result.add(new LockKeyParameter(i, annotation.value()));
            }
        }
        return result;
    }

    /**
     * Returns all the method parameters using parameter names derived via reflection.
     * Useful as a fallback when @LockKeyParam is not present, assuming the code is compiled with -parameters.
     *
     * @return List of parameters info
     */
    public static List<LockKeyParameter> getReflectionMethodParameters(Method method) {
        List<LockKeyParameter> result = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            // Only add if parameter name is actually available (e.g., compiled with -parameters)
            if (parameter.isNamePresent()) {
                result.add(new LockKeyParameter(i, parameter.getName()));
            }
        }
        return result;
    }

}
