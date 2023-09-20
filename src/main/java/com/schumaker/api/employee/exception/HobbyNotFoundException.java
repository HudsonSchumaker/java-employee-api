package com.schumaker.api.employee.exception;

public class HobbyNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Hobby not found";

    public HobbyNotFoundException() {
        super(MESSAGE);
    }
}
