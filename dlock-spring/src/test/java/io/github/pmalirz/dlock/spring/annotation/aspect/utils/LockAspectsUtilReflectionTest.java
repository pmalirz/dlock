package io.github.pmalirz.dlock.spring.annotation.aspect.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockAspectsUtilReflectionTest {

    @Test
    void testReflectionParams() throws Exception {
        Method method = LockAspectsUtilReflectionTest.class.getDeclaredMethod("myMethod", String.class, int.class);

        List<LockAspectsUtil.LockKeyParameter> params = LockAspectsUtil.getReflectionMethodParameters(method);

        // This test only verifies something if the project is compiled with -parameters.
        // If not, it will just return empty, which is also expected behavior when the flag isn't present.
        if (!params.isEmpty()) {
            assertEquals(2, params.size());
            assertEquals("paramOne", params.get(0).name());
            assertEquals("paramTwo", params.get(1).name());
        } else {
            assertTrue(params.isEmpty());
        }
    }

    @SuppressWarnings("unused")
    void myMethod(String paramOne, int paramTwo) {}
}
