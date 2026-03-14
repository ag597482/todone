package com.indra.todone.exception;

import com.indra.todone.dto.response.ApiResponse;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
