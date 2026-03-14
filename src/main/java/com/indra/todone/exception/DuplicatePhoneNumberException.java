package com.indra.todone.exception;

public class DuplicatePhoneNumberException extends RuntimeException {

    public DuplicatePhoneNumberException(String phoneNumber) {
        super("A user with phone number '" + phoneNumber + "' already exists");
    }
}
