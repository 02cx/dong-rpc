package com.dong.exceptions;

public class CompressException extends RuntimeException{
    public CompressException() {
        super();
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
