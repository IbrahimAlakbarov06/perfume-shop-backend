package org.perfume.exception;

import org.perfume.model.enums.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.perfume.model.dto.response.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistsException(AlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .error(ErrorMessage.ALREADY_EXISTS.getDisplayName())
                .message(exception.getMessage())
                .uuid(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .error(ErrorMessage.NOT_FOUND.getDisplayName())
                .message(exception.getMessage())
                .uuid(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .error(ErrorMessage.INVALID_REQUEST.getDisplayName())
                .message(exception.getMessage())
                .uuid(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                .error(ErrorMessage.METHOD_ARGUMENT_NOT_VALID.getDisplayName())
                .message("MethodArgumentNotValidException happened")
                .uuid(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .error(ErrorMessage.INTERNAL_SERVER_ERROR.getDisplayName())
                .message(exception.getMessage())
                .uuid(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .build());
    }
}