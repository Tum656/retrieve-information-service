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
public class MarketStatistics {

    @JsonProperty("stat_date")
    private String statDate;

    @JsonProperty("last_price")
    private Double lastPrice;

    @JsonProperty("market_cap_million")
    private Double marketCapMillion;

    @JsonProperty("ratio_date")
    private String ratioDate;

    @JsonProperty("pe")
    private Double pe;

    @JsonProperty("pbv")
    private Double pbv;

    @JsonProperty("book_value_per_share")
    private Double bookValuePerShare;

    @JsonProperty("dividend_yield_pct")
    private Double dividendYieldPct;
}
