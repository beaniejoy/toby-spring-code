package io.spring.toby.learningtest.jdk;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReflectionTest {
    @Test
    public void invokeMethod() throws Exception {
        String name = "Spring";

        assertThat(name.length(), is(6));

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer) lengthMethod.invoke(name), is(6));

        assertThat(name.charAt(0), is('S'));

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character) charAtMethod.invoke(name, 0), is('S'));

    }

    @Test
    public void simpleProxy() {
        Hello hello = new HelloTarget();
        assertThat(hello.sayHello("beanie"), is("Hello beanie"));
        assertThat(hello.sayHi("beanie"), is("Hi beanie"));
        assertThat(hello.sayThankYou("beanie"), is("Thank You beanie"));

        //Hello proxyHello = new HelloUppercase(new HelloTarget());
        Hello dynamicProxyHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseHandler(new HelloTarget())
        );

        assertThat(dynamicProxyHello.sayHello("beanie"), is("HELLO BEANIE"));
        assertThat(dynamicProxyHello.sayHi("beanie"), is("HI BEANIE"));
        assertThat(dynamicProxyHello.sayThankYou("beanie"), is("THANK YOU BEANIE"));
    }
}
