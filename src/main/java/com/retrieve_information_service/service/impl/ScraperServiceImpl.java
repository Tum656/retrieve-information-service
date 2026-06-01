package com.retrieve_information_service.service.impl;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.retrieve_information_service.constant.ErrorCode;
import com.retrieve_information_service.exception.base.ExceptionHandle;
import com.retrieve_information_service.exception.business.ResourceNotFoundException;
import com.retrieve_information_service.service.ScraperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScraperServiceImpl implements ScraperService {

    private static final String BASE_URL =
            "https://www.set.or.th/th/market/product/stock/quote/";

    @Value("${scraper.timeout.ms:20000}")
    private int timeoutMs;

    @Override
    public String scrapeHtml(String symbol) {
        String url = BASE_URL + symbol.toUpperCase()
                + "/financial-statement/company-highlights";
        log.info("Scraping highlights page for symbol={}", symbol);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(true))) {

            Page page = browser.newPage();
            page.setDefaultTimeout(timeoutMs);

            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.waitForSelector("table",
                    new Page.WaitForSelectorOptions().setTimeout(timeoutMs));

            // Allow React to finish populating financial table cells
            page.waitForTimeout(2000);

            String html = page.content();

            if (page.title().contains("ไม่พบ") || html.contains("ไม่พบหุ้น")) {
                throw new ResourceNotFoundException(ErrorCode.TICKER_NOT_FOUND,
                        "Ticker not found on SET: " + symbol);
            }

            log.info("Scrape complete: symbol={} chars={}", symbol, html.length());
            return html;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (TimeoutError e) {
            log.error("Playwright timeout for symbol={}: {}", symbol, e.getMessage());
            throw new ExceptionHandle(ErrorCode.SCRAPER_ERROR,
                    "Timed out after " + timeoutMs + "ms for: " + symbol,
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected scraper error for symbol={}", symbol, e);
            throw new ExceptionHandle(ErrorCode.SCRAPER_ERROR,
                    "Scraper error for symbol: " + symbol,
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}