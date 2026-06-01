package com.retrieve_information_service.exception.base;

import com.retrieve_information_service.constant.ErrorCode;
import com.retrieve_information_service.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Generic one-off exception for cases where creating a dedicated subclass is unnecessary.
 * Extends {@link BusinessException} so it is automatically handled by
 * the single {@code handleBaseException} in {@code GlobalExceptionHandler}.
 *
 * <p>Prefer specific subclasses when the same error type is thrown from multiple places.
 *
 * <pre>
 *   throw new ExceptionHandle(ErrorCode.SCRAPER_ERROR, "Rate limit hit", HttpStatus.TOO_MANY_REQUESTS);
 * </pre>
 */
public class ExceptionHandle extends BusinessException {

    public ExceptionHandle(ErrorCode.Detail error, HttpStatus status) {
        super(error, status);
    }

    public ExceptionHandle(ErrorCode.Detail error, String customMessage, HttpStatus status) {
        super(error, customMessage, status);
    }
}
