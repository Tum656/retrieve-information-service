package com.retrieve_information_service.exception.handler;

import com.retrieve_information_service.exception.base.BaseException;
import com.retrieve_information_service.exception.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ALL {@link BaseException} subclasses
     * ({@code BusinessException}, {@code ResourceNotFoundException},
     * {@code ServiceException}, {@code ExceptionHandle}, and any domain exception).
     * HTTP status and error code are read dynamically from the exception —
     * no per-subclass handler needed.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        HttpStatus status = ex.getHttpStatus();
        log.warn("Application exception: status={} code={} message={}",
                status, ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .code(ex.getErrorCode())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /** Handles {@code @Valid} / {@code @Validated} failures — always HTTP 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(400).error("Bad Request")
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /** Catch-all — never leaks internal details. Always HTTP 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(500).error("Internal Server Error")
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
