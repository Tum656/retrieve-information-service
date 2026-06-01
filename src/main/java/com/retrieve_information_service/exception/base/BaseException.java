package com.retrieve_information_service.exception.base;

import com.retrieve_information_service.constant.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Abstract base for all custom exceptions.
 * Carries {@link HttpStatus} and an error code so {@code GlobalExceptionHandler}
 * resolves status and code dynamically — no per-subclass handler needed.
 */
public abstract class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected BaseException(ErrorCode.Detail error, HttpStatus httpStatus) {
        super(error.message());
        this.errorCode  = error.code();
        this.httpStatus = httpStatus;
    }

    protected BaseException(ErrorCode.Detail error, String customMessage, HttpStatus httpStatus) {
        super(customMessage);
        this.errorCode  = error.code();
        this.httpStatus = httpStatus;
    }

    protected BaseException(ErrorCode.Detail error, HttpStatus httpStatus, Throwable cause) {
        super(error.message(), cause);
        this.errorCode  = error.code();
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode()      { return errorCode; }
}
