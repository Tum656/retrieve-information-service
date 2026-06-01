package com.retrieve_information_service.controller;


import com.retrieve_information_service.dto.StockHighlightsResponse;
import com.retrieve_information_service.service.ParserService;
import com.retrieve_information_service.service.ScraperService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final ScraperService scraperService;   // inject through interface, not implementation
    private final ParserService parserService;     // inject through interface, not implementation

    /**
     * Returns company highlights (financial statements, ratios, market statistics)
     * for the given stock symbol scraped live from the SET website.
     *
     * @param symbol stock symbol e.g. TPIPP
     * @param period "annual" or "quarterly"
     * @param years  number of periods to include (1–10, default 5)
     */
    @GetMapping(
            path = "/{symbol}/highlights",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<StockHighlightsResponse> getHighlights(
            @PathVariable
            @NotBlank(message = "symbol must not be blank")
            String symbol,

            @RequestParam(defaultValue = "annual")
            @Pattern(regexp = "annual|quarterly",
                     message = "period must be 'annual' or 'quarterly'")
            String period,

            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "years must be >= 1")
            @Max(value = 10, message = "years must be <= 10")
            Integer years) {

        log.info("GET highlights — symbol={} period={} years={}", symbol, period, years);

        String html = scraperService.scrapeHtml(symbol);
        return ResponseEntity.ok(parserService.parse(html, symbol));
    }
}
