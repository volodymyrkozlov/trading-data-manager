package com.volodymyrkozlov.tradingdatamanager.service;

import com.volodymyrkozlov.tradingdatamanager.dto.FinancialDataResponse;
import com.volodymyrkozlov.tradingdatamanager.repository.SymbolTradingDataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.volodymyrkozlov.tradingdatamanager.dto.FinancialDataResponse.financialDataResponseBuilder;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.averageTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.lastTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.maxTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.minTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.varianceTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.utils.MathUtils.powerOfTen;

@Service
public class SymbolFinancialDataService {
    private final SymbolTradingDataRepository repository;
    private final Integer maxKValue;

    public SymbolFinancialDataService(SymbolTradingDataRepository repository,
                                      @Value("${max-k-value}") Integer maxKValue) {
        this.repository = repository;
        this.maxKValue = maxKValue;
    }

    public void addFinancialData(String symbol,
                                 List<Double> symbolTradingPrices) {
        repository.addSymbolTradingData(symbol, symbolTradingPrices);
    }

    public FinancialDataResponse getFinancialData(String symbol,
                                                  int k) {
        validateMaxKValue(k);
        final var analyzePoints = powerOfTen(k);

        final var tradingPricingData = repository.getTradingData(symbol);

        final var tradingPrices = tradingPricingData.tradingPrices();
        final var tradingPricesPrefixSums = tradingPricingData.tradingPricesPrefixSums();
        final var tradingPricesPrefixSquares = tradingPricingData.tradingPricesPrefixSquares();
        final var maxDequeues = tradingPricingData.maxDequeues();
        final var minDequeues = tradingPricingData.minDequeues();

        return financialDataResponseBuilder()
                .last(lastTradingPrice(tradingPrices))
                .avg(averageTradingPrice(tradingPricesPrefixSums, analyzePoints))
                .max(maxTradingPrice(tradingPrices, maxDequeues, analyzePoints))
                .min(minTradingPrice(tradingPrices, minDequeues, analyzePoints))
                .var(varianceTradingPrice(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, analyzePoints))
                .build();
    }

    private void validateMaxKValue(int k) {
        if (k > maxKValue) {
            throw new IllegalArgumentException("K value %s is greater than allowed %s".formatted(k, maxKValue));
        }
    }
}
