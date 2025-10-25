package org.example.eventmanagermodule.Location.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.example.eventmanagermodule.Location.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class})
    public ResponseEntity<ErrorMessageResponse> handleValidationException(Exception e, HttpServletRequest request) {
        String detailedMessage;
        if (e instanceof MethodArgumentNotValidException ex) {
            detailedMessage = constructMethodArgumentNotValid(ex);
        } else {
            detailedMessage = e.getMessage();
        }
        log.warn("400 BAD_REQUEST at {} caused by {}: {}", request.getRequestURI(), e.getClass().getSimpleName(), detailedMessage);
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, detailedMessage);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleUsernameNotFoundException(Exception e, HttpServletRequest request) {
        log.warn("401 UNAUTHORIZED at {}: {}", request.getRequestURI(), e.getMessage());
        String detailedMessage = e.getMessage();
        return buildErrorResponse(request, HttpStatus.UNAUTHORIZED, detailedMessage);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        log.warn("404 NOT_FOUND at {}: {}", request.getRequestURI(), e.getMessage());
        String detailedMessage = e.getMessage();
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, detailedMessage);
    }

    @ExceptionHandler (DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessageResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("409 CONFLICT at {}: {}", request.getRequestURI(), e.getMessage());// не уверен в статусе 500, не понял когда прилетает DataIntegrityViolationException
        String detailedMessage = e.getMessage();
        return buildErrorResponse(request, HttpStatus.CONFLICT, detailedMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("500 INTERNAL_SERVER_ERROR at {}: {}", request.getRequestURI(), e.getMessage(), e);
        String detailedMessage = e.getMessage();
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, detailedMessage);
    }

    private String constructMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(","));
    }

    private ResponseEntity<ErrorMessageResponse> buildErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            String message) {
        log.info("{} at {}: {}", status, request.getRequestURI(), message);
        var errorDto = new ErrorMessageResponse(status.name(), message, LocalDateTime.now());
        return ResponseEntity
                .status(status)
                .body(errorDto);
    }
}
