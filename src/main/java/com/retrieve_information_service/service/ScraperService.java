package com.retrieve_information_service.service;


/**
 * Contract for scraping the SET company-highlights page using a headless browser.
 */
public interface ScraperService {

    /**
     * Opens the SET company-highlights page for the given symbol,
     * waits for financial tables to finish rendering, and returns the full page HTML.
     *
     * @param symbol stock symbol e.g. TPIPP
     * @return full rendered HTML of the page
     */
    String scrapeHtml(String symbol);
}
