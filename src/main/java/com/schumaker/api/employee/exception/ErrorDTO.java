package com.schumaker.api.employee.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ErrorDTO {

    private String title;
    private String path;
    private HttpStatus status;
    private LocalDateTime timestamp = LocalDateTime.now();
}
