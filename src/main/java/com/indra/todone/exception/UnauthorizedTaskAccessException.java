package com.indra.todone.exception;

public class UnauthorizedTaskAccessException extends RuntimeException {

    public UnauthorizedTaskAccessException(String message) {
        super(message != null ? message : "Only the task author can perform this action.");
    }
}
