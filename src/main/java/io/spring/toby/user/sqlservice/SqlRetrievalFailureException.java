package io.spring.toby.user.sqlservice;

public class SqlRetrievalFailureException extends RuntimeException{
    public SqlRetrievalFailureException(String message) {
        super(message);
    }

    /**
     * @param cause SQL 가져오는데 실패한 근본 원인을 담을 수 있도록 중첩 예외 설정
     */
    public SqlRetrievalFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlRetrievalFailureException(RuntimeException e) {
        super(e);
    }
}
