package com.schumaker.api.employee.exception;

import lombok.Data;

@Data
public class ValidationFormDTO {

    private String field;
    private String error;

    public ValidationFormDTO(String field, String error) {
        this.field = field;
        this.error = error;
    }
}
