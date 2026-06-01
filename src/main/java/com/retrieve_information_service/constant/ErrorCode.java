package com.retrieve_information_service.constant;

public final class ErrorCode {
    private ErrorCode() {}

    public record Detail(String code, String message) {}

    // --- Factsheet ---
    public static final Detail TICKER_NOT_FOUND      = new Detail("FACTSHEET-001", "Stock ticker not found on SET");
    public static final Detail FACTSHEET_PARSE_ERROR = new Detail("FACTSHEET-002", "Failed to parse factsheet data");
    public static final Detail SCRAPER_ERROR         = new Detail("FACTSHEET-003", "Unexpected error while scraping SET website");

    // --- System ---
    public static final Detail INTERNAL_ERROR        = new Detail("ERR-500", "Internal server error");
}
