package org.example.eventmanagermodule.Location.web;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.eventmanagermodule.Location.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

//400 BAD REQUEST
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorMessageResponse> handleValidationException(Exception e, HttpServletRequest request) {
        String detailedMessage = e instanceof MethodArgumentNotValidException
                ?
                constructMethodArgumentNotValid((MethodArgumentNotValidException) e)
                :
                e.getMessage();
        log.error("400 BAD_REQUEST at {}: {}", request.getRequestURI(), detailedMessage);
        var errorDto = new ErrorMessageResponse(
                "400 BAD_REQUEST",
                detailedMessage,
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    //401 UNAUTHORIZED
    //BadCredentialsException - — когда неверный логин/пароль при аутентификации.
    // UsernameNotFoundException - когда пытаются аутентифицироваться несуществующим пользователем (может совпадать с BadCredentials).
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorMessageResponse> handleBadCredentialsException(Exception e, HttpServletRequest request) {
        log.error("401 UNAUTHORIZED at {}: {}", request.getRequestURI(), e.getMessage());//неверный логин/пароль при аутентификации.
        var errorDto = new ErrorMessageResponse(
                "401 UNAUTHORIZED",// неверный логин/пароль
                " Incorrect login or password",
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorDto);
    }

    //403 FORBIDDEN
    //AuthorizationDeniedException / AccessDeniedException
    // если внутри сервисов есть проверки прав, которые не связаны напрямую с фильтром SecurityFilterChain.
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorMessageResponse> handleDeniedException(Exception e, HttpServletRequest request) {
        log.error("403 FORBIDDEN at {}: {}", request.getRequestURI(), e.getMessage());
        var errorDto = new ErrorMessageResponse(
                "403 - FORBIDDEN",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorDto);
    }

    //404 NOT FOUND
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        log.error("404 NOT_FOUND at {}: {}", request.getRequestURI(), e.getMessage());

        var errorDto =
                new ErrorMessageResponse(
                        "404 NOT_FOUND",
                        e.getMessage(),
                        LocalDateTime.now()
                );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    // 500 INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("500 INTERNAL_SERVER_ERROR at {}: {}", request.getRequestURI(), e.getMessage(), e);
        var errorDto = new ErrorMessageResponse(
                "500 INTERNAL_SERVER_ERROR",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }


    private String constructMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(","));
    }
}
