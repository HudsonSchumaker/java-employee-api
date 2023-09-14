package com.schumaker.api.employee.exception;

public class EmployeeNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Employee not found";

    public EmployeeNotFoundException() {
        super(MESSAGE);
    }
}
