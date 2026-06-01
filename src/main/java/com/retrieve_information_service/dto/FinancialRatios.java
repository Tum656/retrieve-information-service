package com.retrieve_information_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRatios {

    @JsonProperty("period_label")
    private String periodLabel;

    @JsonProperty("as_of_date")
    private String asOfDate;

    @JsonProperty("roa_pct")
    private Double roaPct;

    @JsonProperty("roe_pct")
    private Double roePct;

    @JsonProperty("net_profit_margin_pct")
    private Double netProfitMarginPct;
}
