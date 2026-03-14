package com.indra.todone.exception;

import com.indra.todone.dto.response.ApiResponse;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(HttpMessageNotReadableException ex) {
        String rawMessage = ex.getMessage() != null ? ex.getMessage() : "";
        String message;
        if (rawMessage.contains("required request body is missing")) {
            message = "Request body is required";
        } else if (rawMessage.contains("TaskStatus") || rawMessage.contains("taskStatus")) {
            message = "Invalid task status. Allowed values: PENDING, COMPLETED";
        } else {
            message = "Invalid request body. Ensure valid JSON with required fields (e.g. sessionId, otp for verify; phoneNumber for send OTP).";
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOtp(InvalidOtpException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedTaskAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedTaskAccess(UnauthorizedTaskAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(DuplicatePhoneNumberException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicatePhoneNumber(DuplicatePhoneNumberException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleNonUniqueResult(IncorrectResultSizeDataAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("Multiple users found for the same phone number. Please contact support."));
    }
}
