package io.spring.toby.learningtest.jdk.proxy;

import io.spring.toby.learningtest.jdk.Hello;
import io.spring.toby.learningtest.jdk.HelloTarget;
import io.spring.toby.learningtest.jdk.UppercaseAdvice;
import io.spring.toby.learningtest.jdk.UppercaseHandler;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DynamicProxyTest {
    @Test
    public void simpleProxy() {
        Hello proxyHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseHandler(new HelloTarget())
        );
    }

    @Test
    public void proxyFactoryBean() {
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        pfBean.addAdvice(new UppercaseAdvice());

        Hello proxyHello = (Hello) pfBean.getObject();

        assertThat(proxyHello.sayHello("beanie"), is("HELLO BEANIE"));
        assertThat(proxyHello.sayHi("beanie"), is("HI BEANIE"));
        assertThat(proxyHello.sayThankYou("beanie"), is("THANK YOU BEANIE"));
    }

    @Test
    public void pointcutAdvisor() {
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");

        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxyHello = (Hello) pfBean.getObject();

        assertThat(proxyHello.sayHello("beanie"), is("HELLO BEANIE"));
        assertThat(proxyHello.sayHi("beanie"), is("HI BEANIE"));
        assertThat(proxyHello.sayThankYou("beanie"), is("Thank You beanie")); // advice 적용 X
    }
}
