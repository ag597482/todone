package com.indra.todone.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message != null ? message : "User not found.");
    }
}
