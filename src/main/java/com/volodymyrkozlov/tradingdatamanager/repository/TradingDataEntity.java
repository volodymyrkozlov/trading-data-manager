package com.volodymyrkozlov.tradingdatamanager.repository;

import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;

public record TradingDataEntity(List<Double> tradingPrices,
                                List<Double> tradingPricesPrefixSums,
                                List<Double> tradingPricesPrefixSquares,
                                Map<Integer, Deque<Integer>> maxDequeues,
                                Map<Integer, Deque<Integer>> minDequeues) {
    public TradingDataEntity {
        validateRequired(tradingPrices, "tradingPrices");
        validateRequired(tradingPricesPrefixSums, "tradingPricesPrefixSums");
        validateRequired(tradingPricesPrefixSquares, "tradingPricesPrefixSquares");
        validateRequired(maxDequeues, "maxDequeues");
        validateRequired(minDequeues, "minDequeues");
    }

    public static Builder tradingDataEntityBuilder() {
        return new Builder();
    }

    public static class Builder {
        private List<Double> tradingPrices;
        private List<Double> tradingPricesPrefixSums;
        private List<Double> tradingPricesPrefixSquares;
        private Map<Integer, Deque<Integer>> maxDequeues;
        private Map<Integer, Deque<Integer>> minDequeues;

        public Builder tradingPrices(List<Double> tradingPrices) {
            this.tradingPrices = tradingPrices;
            return this;
        }

        public Builder tradingPricesPrefixSums(List<Double> tradingPricesPrefixSums) {
            this.tradingPricesPrefixSums = tradingPricesPrefixSums;
            return this;
        }

        public Builder tradingPricesPrefixSquares(List<Double> tradingPricesPrefixSquares) {
            this.tradingPricesPrefixSquares = tradingPricesPrefixSquares;
            return this;
        }

        public Builder maxDeque(Map<Integer, Deque<Integer>> maxDequeues) {
            this.maxDequeues = maxDequeues;
            return this;
        }

        public Builder minDeque(Map<Integer, Deque<Integer>> minDequeues) {
            this.minDequeues = minDequeues;
            return this;
        }

        public TradingDataEntity build() {
            return new TradingDataEntity(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, maxDequeues, minDequeues);
        }
    }
}
