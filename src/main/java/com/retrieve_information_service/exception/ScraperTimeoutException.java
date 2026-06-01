package com.retrieve_information_service.exception;

/** Thrown when Playwright times out waiting for the SET page to render financial tables. */
public class ScraperTimeoutException extends RuntimeException {

    public ScraperTimeoutException() {
        super("Scraper timed out waiting for SET page to load");
    }

    public ScraperTimeoutException(String message) {
        super(message);
    }
}
