package com.dong.exceptions;

public class NetworkException extends RuntimeException{

    public NetworkException() {
        super();
    }

    public NetworkException(String message) {
        super(message);
    }
}
