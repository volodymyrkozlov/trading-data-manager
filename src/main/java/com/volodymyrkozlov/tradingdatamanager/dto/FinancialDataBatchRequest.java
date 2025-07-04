package com.volodymyrkozlov.tradingdatamanager.dto;

import java.util.List;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;

public record FinancialDataBatchRequest(String symbol,
                                        List<Double> values) {

    public FinancialDataBatchRequest {
        validateRequired(symbol, "symbol");
        validateRequired(symbol, "values");
    }
}
