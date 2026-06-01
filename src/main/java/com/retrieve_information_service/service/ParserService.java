package com.retrieve_information_service.service;


import com.retrieve_information_service.dto.StockHighlightsResponse;

/**
 * Contract for parsing SET company-highlights HTML into structured financial data.
 */
public interface ParserService {

    /**
     * Parses the rendered HTML of the SET company-highlights page into a structured
     * response containing financial statements, ratios, and market statistics.
     *
     * @param html   full page HTML returned by {@link ScraperService}
     * @param symbol stock symbol used for response metadata
     * @return structured highlights response
     */
    StockHighlightsResponse parse(String html, String symbol);
}
