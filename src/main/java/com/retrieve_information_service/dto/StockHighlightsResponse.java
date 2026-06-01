package com.retrieve_information_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHighlightsResponse {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("generated_at")
    private String generatedAt;

    @JsonProperty("financial_statements")
    private List<FinancialStatement> financialStatements;

    @JsonProperty("financial_ratios")
    private List<FinancialRatios> financialRatios;

    @JsonProperty("market_statistics")
    private List<MarketStatistics> marketStatistics;
}
