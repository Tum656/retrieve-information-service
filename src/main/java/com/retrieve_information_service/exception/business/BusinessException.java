package com.retrieve_information_service.exception.business;

import com.retrieve_information_service.constant.ErrorCode;
import com.retrieve_information_service.exception.base.BaseException;
import org.springframework.http.HttpStatus;

/** Base for business-rule violations. Defaults to HTTP 400; status is overridable at throw site. */
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode.Detail error) {
        super(error, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(ErrorCode.Detail error, HttpStatus status) {
        super(error, status);
    }

    public BusinessException(ErrorCode.Detail error, String customMessage, HttpStatus status) {
        super(error, customMessage, status);
    }
}
