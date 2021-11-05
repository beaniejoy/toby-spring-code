package io.spring.toby.user.sqlservice;

public class SqlNotFoundException extends RuntimeException{
    public SqlNotFoundException(String message) {
        super(message);
    }
}
