package com.indra.todone.exception;

public class SubtaskNotFoundException extends RuntimeException {

    public SubtaskNotFoundException(String message) {
        super(message != null ? message : "Subtask not found.");
    }
}
