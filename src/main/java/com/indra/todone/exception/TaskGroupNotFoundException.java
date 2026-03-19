package com.indra.todone.exception;

public class TaskGroupNotFoundException extends RuntimeException {

    public TaskGroupNotFoundException(String message) {
        super(message != null ? message : "Task group not found.");
    }
}

