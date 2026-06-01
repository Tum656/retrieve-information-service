package com.retrieve_information_service.exception.business;

import com.retrieve_information_service.constant.ErrorCode;
import org.springframework.http.HttpStatus;

/** Thrown when a requested resource does not exist. Always HTTP 404. */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode.Detail error) {
        super(error, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(ErrorCode.Detail error, String customMessage) {
        super(error, customMessage, HttpStatus.NOT_FOUND);
    }
}
