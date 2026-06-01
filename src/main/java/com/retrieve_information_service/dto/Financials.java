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
public class Financials {

    @JsonProperty("total_assets")
    private Double totalAssets;

    @JsonProperty("total_liabilities")
    private Double totalLiabilities;

    @JsonProperty("shareholders_equity")
    private Double shareholdersEquity;

    @JsonProperty("paid_up_capital")
    private Double paidUpCapital;

    @JsonProperty("total_revenue")
    private Double totalRevenue;

    @JsonProperty("other_income")
    private Double otherIncome;

    @JsonProperty("net_profit")
    private Double netProfit;

    @JsonProperty("eps")
    private Double eps;
}
