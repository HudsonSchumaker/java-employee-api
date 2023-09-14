package com.schumaker.api.employee.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    private static final String MESSAGE = "Email already in exists";

    public EmailAlreadyExistsException() {
        super(MESSAGE);
    }
}
