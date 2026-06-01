package com.retrieve_information_service.exception.base;

import org.springframework.http.HttpStatus;

/**
 * Base for all custom exceptions.
 * Carries an {@link HttpStatus} so {@code GlobalExceptionHandler} can resolve
 * the response status dynamically — no per-subclass handler needed.
 *
 * <pre>
 *   // throw with any status at the call site:
 *   throw new SomeException("ticker not found", HttpStatus.NOT_FOUND);
 *
 *   // handler reads it back:
 *   ex.getHttpStatus()  →  404 NOT_FOUND
 * </pre>
 */
public class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    protected BaseException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}