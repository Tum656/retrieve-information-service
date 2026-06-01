package com.retrieve_information_service.exception;

/** Thrown when the requested stock symbol does not exist on SET website. */
public class SymbolNotFoundException extends RuntimeException {

    private final String symbol;

    public SymbolNotFoundException(String symbol) {
        super("Stock symbol not found on SET: " + symbol);
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
