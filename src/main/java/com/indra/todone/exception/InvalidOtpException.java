package com.indra.todone.exception;

public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String message) {
        super(message != null ? message : "Invalid or expired OTP");
    }
}
