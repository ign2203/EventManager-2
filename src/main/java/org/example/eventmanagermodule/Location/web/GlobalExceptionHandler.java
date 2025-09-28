package org.example.eventmanagermodule.Location.web;


import jakarta.persistence.EntityNotFoundException;
import org.example.eventmanagermodule.Location.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorMessageResponse> handleValidationException(Exception e) {
        log.error("400 Bad Request/ Некорректный запроc", e);
        String detailedMessage = e instanceof MethodArgumentNotValidException
                ?
                constructMethodArgumentNotValid((MethodArgumentNotValidException) e)
                :
                e.getMessage();

        var errorDto = new ErrorMessageResponse(
                "Некорректный запрос",
                detailedMessage,
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleNotFoundException(EntityNotFoundException e) {
        log.error("404 Not Found/Cущность не найдена", e);

        var errorDto =
                new ErrorMessageResponse(
                        "Сущность не найдена",
                        e.getMessage(),
                        LocalDateTime.now()
                );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorMessageResponse> handleGenericException(Exception e) {
        log.error("500 Internal Server Error/Внутренняя ошибка сервера", e);
        var errorDto = new ErrorMessageResponse(
                "Внутренняя ошибка сервера",
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