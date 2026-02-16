package com.dlock.spring.annotation.aspect.utils;

import com.dlock.spring.annotation.LockKeyParam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

class LockAspectsUtilTest {

    @Test
    void getLockKeyMethodParameters_AllParameters() throws NoSuchMethodException {
        // given:
        Method targetMethod = LockAspectsUtilTest.class.getDeclaredMethod("testMethodParams", int.class, String.class);

        // when:
        List<LockAspectsUtil.LockKeyParameter> parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod);

        // then:
        assertThat(parameters, hasSize(2));
        assertThat(parameters, containsInAnyOrder(
                new LockAspectsUtil.LockKeyParameter(0, "aParam"),
                new LockAspectsUtil.LockKeyParameter(1, "bParam")));
    }

    @Test
    void getLockKeyMethodParameters_PartParameters() throws NoSuchMethodException {
        // given:
        Method targetMethod = LockAspectsUtilTest.class.getDeclaredMethod("testMethodPartParams", int.class,
                String.class);

        // when:
        List<LockAspectsUtil.LockKeyParameter> parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod);

        // then:
        assertThat(parameters, hasSize(1));
        assertThat(parameters, containsInAnyOrder(
                new LockAspectsUtil.LockKeyParameter(0, "aParam")));
    }

    @Test
    void getLockKeyMethodParameters_NoParameters() throws NoSuchMethodException {
        // given:
        Method targetMethod = LockAspectsUtilTest.class.getDeclaredMethod("testMethodNoParams");

        // when:
        List<LockAspectsUtil.LockKeyParameter> parameters = LockAspectsUtil.getLockKeyMethodParameters(targetMethod);

        // then:
        assertThat(parameters, hasSize(0));
    }

    @SuppressWarnings("unused")
    private void testMethodParams(@LockKeyParam("aParam") int a, @LockKeyParam("bParam") String b) {
    }

    @SuppressWarnings("unused")
    private void testMethodPartParams(@LockKeyParam("aParam") int a, String b) {
    }

    @SuppressWarnings("unused")
    private void testMethodNoParams() {
    }

}
