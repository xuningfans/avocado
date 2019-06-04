package com.avocado.common.exception;

/**
 * AvocadoException class
 *
 * @author xuning
 * @date 2019-06-04 17:06
 */
public class AvocadoException extends RuntimeException {
    public AvocadoException() {
        super();
    }

    public AvocadoException(String message) {
        super(message);
    }

    public AvocadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AvocadoException(Throwable cause) {
        super(cause);
    }

    protected AvocadoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
