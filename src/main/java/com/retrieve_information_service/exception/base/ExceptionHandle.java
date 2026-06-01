package com.retrieve_information_service.exception.base;

import lombok.Data;
import org.springframework.http.HttpStatus;

/** Thrown when the requested stock symbol does not exist on SET website. */
@Data
public class ExceptionHandle extends RuntimeException {

    private final String symbol;
    private final HttpStatus httpStatus;

    public ExceptionHandle(String symbol, HttpStatus httpStatus) {
        super("Stock symbol not found on SET: " + symbol);
        this.symbol = symbol;
        this.httpStatus = httpStatus;
    }

}
