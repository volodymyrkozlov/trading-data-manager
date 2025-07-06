package com.volodymyrkozlov.tradingdatamanager.repository;

import java.util.Deque;
import java.util.Map;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;

public record TradingDataEntity(DoubleRingBuffer tradingPrices,
                                DoubleRingBuffer tradingPricesPrefixSums,
                                DoubleRingBuffer tradingPricesPrefixSquares,
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
        private DoubleRingBuffer tradingPrices;
        private DoubleRingBuffer tradingPricesPrefixSums;
        private DoubleRingBuffer tradingPricesPrefixSquares;
        private Map<Integer, Deque<Integer>> maxDequeues;
        private Map<Integer, Deque<Integer>> minDequeues;

        public Builder tradingPrices(DoubleRingBuffer tradingPrices) {
            this.tradingPrices = tradingPrices;
            return this;
        }

        public Builder tradingPricesPrefixSums(DoubleRingBuffer tradingPricesPrefixSums) {
            this.tradingPricesPrefixSums = tradingPricesPrefixSums;
            return this;
        }

        public Builder tradingPricesPrefixSquares(DoubleRingBuffer tradingPricesPrefixSquares) {
            this.tradingPricesPrefixSquares = tradingPricesPrefixSquares;
            return this;
        }

        public Builder maxDequeues(Map<Integer, Deque<Integer>> maxDequeues) {
            this.maxDequeues = maxDequeues;
            return this;
        }

        public Builder minDequeues(Map<Integer, Deque<Integer>> minDequeues) {
            this.minDequeues = minDequeues;
            return this;
        }

        public TradingDataEntity build() {
            return new TradingDataEntity(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, maxDequeues, minDequeues);
        }
    }
}
