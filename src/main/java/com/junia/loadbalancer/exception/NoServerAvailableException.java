package com.junia.loadbalancer.exception;

public class NoServerAvailableException extends Exception {

    public NoServerAvailableException(String message) {
        super(message);
    }
}
