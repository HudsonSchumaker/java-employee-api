package com.schumaker.api.employee.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public final class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public List<ValidationFormDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ValidationFormDTO> errors = new ArrayList<>();

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        fieldErrors.forEach(e -> {
            String message = messageSource.getMessage(e, LocaleContextHolder.getLocale());
            ValidationFormDTO error = new ValidationFormDTO(e.getField(), message);
            errors.add(error);
        });

        return errors;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO();
        error.setTitle(ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO();
        error.setTitle(ex.getMessage());
        error.setStatus(HttpStatus.CONFLICT);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleEmployeeNotFoundException(EmployeeNotFoundException ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO();
        error.setTitle(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HobbyNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleHobbyNotFoundException(HobbyNotFoundException ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO();
        error.setTitle(ex.getMessage());
        error.setStatus(HttpStatus.NOT_FOUND);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorDTO> handleDateTimeParseException(DateTimeParseException ex, HttpServletRequest request) {
        ErrorDTO error = new ErrorDTO();
        error.setTitle("Use format YYYY-MM-dd, " + ex.getMessage());
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setPath(request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
