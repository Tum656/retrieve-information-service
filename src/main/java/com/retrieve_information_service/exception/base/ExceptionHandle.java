package com.retrieve_information_service.exception.base;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ExceptionHandle extends RuntimeException {

    private final String message;
    private final HttpStatus httpStatus;

    public ExceptionHandle(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
