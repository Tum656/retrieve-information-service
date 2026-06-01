package com.retrieve_information_service.exception.system;

import com.retrieve_information_service.constant.ErrorCode;
import com.retrieve_information_service.exception.base.BaseException;
import org.springframework.http.HttpStatus;

/** Infrastructure / unexpected errors. Always HTTP 500. */
public class ServiceException extends BaseException {

    public ServiceException(ErrorCode.Detail error, Throwable cause) {
        super(error, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
