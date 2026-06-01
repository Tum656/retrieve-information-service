package com.retrieve_information_service.exception.handler;

import com.retrieve_information_service.exception.base.BaseException;
import com.retrieve_information_service.exception.base.ExceptionHandle;
import com.retrieve_information_service.exception.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Catch-all for any unhandled exception — always HTTP 500.
     * Does not leak internal details to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> Exception(Exception ex,BaseException baseException) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(baseException.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(baseException.getHttpStatus().value())
                        .error(baseException.getHttpStatus().getReasonPhrase())
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ExceptionHandle.class)
    public ResponseEntity<ErrorResponse> ExceptionHandle(ExceptionHandle ex) {
        log.warn("Symbol not found: {}", ex.getSymbol());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(ex.getHttpStatus().value())
                        .error(ex.getHttpStatus().getReasonPhrase())
                        .message(ex.getSymbol())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
