package io.spring.toby.learningtest.spring.pointcut;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PointcutExpressionTest {
    @Test
    public void methodSignaturePointcut() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int "
                + "io.spring.toby.learningtest.spring.pointcut.Target.minus(int,int) "
                + "throws java.lang.RuntimeException)");

        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("minus", int.class, int.class), null)
                , is(true));

        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                        pointcut.getMethodMatcher().matches(
                                Target.class.getMethod("plus", int.class, int.class), null)
                , is(false));

        assertThat(pointcut.getClassFilter().matches(Bean.class) &&
                        pointcut.getMethodMatcher().matches(
                                Target.class.getMethod("method"), null)
                , is(false));
    }

    @Test
    public void print() throws NoSuchMethodException {
        System.out.println(Target.class.getMethod("minus", int.class, int.class));
    }
}
