package com.volodymyrkozlov.tradingdatamanager.service;

import com.volodymyrkozlov.tradingdatamanager.repository.DoubleRingBuffer;

import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

class SymbolFinancialDataAnalyzer {

    /**
     * Time complexity is O(n)
     * Space complexity is O(n)
     */
    static double lastTradingPrice(DoubleRingBuffer tradingPrices) {
        validateNotNull(tradingPrices);
        return tradingPrices.getByIndex(tradingPrices.currentIndex());
    }

    /**
     * Time complexity is O(n)
     * Space complexity is O(n)
     */
    static double averageTradingPrice(DoubleRingBuffer tradingPricesPrefixSums,
                                      int k) {
        validateNotNull(tradingPricesPrefixSums);

        final var end = tradingPricesPrefixSums.currentIndex();
        final var size = tradingPricesPrefixSums.size();
        final var elements = Math.min(k, size);
        final var start = end - elements + 1;

        final var total = tradingPricesPrefixSums.getByIndex(end);
        final var excluded = (start > 0) ? tradingPricesPrefixSums.getByIndex(start - 1) : 0.0;

        return (total - excluded) / elements;
    }

    /**
     * Time complexity is O(n)
     * Space complexity is O(n)
     */
    static double varianceTradingPrice(DoubleRingBuffer tradingPrices,
                                       DoubleRingBuffer tradingPricesPrefixSums,
                                       DoubleRingBuffer tradingPricesPrefixSquares,
                                       int k) {
        validateNotNull(tradingPrices);
        validateNotNull(tradingPricesPrefixSums);
        validateNotNull(tradingPricesPrefixSquares);

        validateSameSize(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares);

        final var end = tradingPrices.currentIndex();
        final var size = tradingPrices.size();
        final var elements = Math.min(k, size);
        final var start = end - elements + 1;

        if (elements == 1) return 0.0;

        var sum = tradingPricesPrefixSums.getByIndex(end);
        var sumSq = tradingPricesPrefixSquares.getByIndex(end);

        if (start > 0) {
            sum -= tradingPricesPrefixSums.getByIndex(start - 1);
            sumSq -= tradingPricesPrefixSquares.getByIndex(start - 1);
        }

        return (elements == size)
                ? populationVariance(sumSq, sum, elements)
                : sampleVariance(sumSq, sum, elements);
    }

    /**
     * Time complexity is O(n)
     * Space complexity is O(n)
     */
    static double minTradingPrice(DoubleRingBuffer prices,
                                  Map<Integer, Deque<Integer>> minDequeues,
                                  int k) {
        return resolveDequeStats(prices, minDequeues, k);
    }

    /**
     * Time complexity is O(n)
     * Space complexity is O(n)
     */
    static double maxTradingPrice(DoubleRingBuffer prices,
                                  Map<Integer, Deque<Integer>> maxDequeues,
                                  int k) {
        return resolveDequeStats(prices, maxDequeues, k);
    }

    private static double resolveDequeStats(DoubleRingBuffer prices,
                                            Map<Integer, Deque<Integer>> dequeues,
                                            int k) {
        final var deque = Optional.ofNullable(dequeues.get(k))
                .orElseThrow(() -> new IllegalArgumentException("Dequeues don't contain provided K %s".formatted(k)));

        if (deque.isEmpty()) {
            throw new IllegalArgumentException("Deque for K %s doesn't contain any data".formatted(k));
        }

        final var index = deque.peekFirst();

        try {
            return prices.getByIndex(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Trading prices don't contain index %s from deque".formatted(index));
        }
    }

    private static double sampleVariance(Double sumSq,
                                         Double sum,
                                         int elementsAnalyze) {
        return (sumSq - (sum * sum) / elementsAnalyze) / (elementsAnalyze - 1);
    }

    private static double populationVariance(Double sumSq,
                                             Double sum,
                                             int elementsAnalyze) {
        final var mean = sum / elementsAnalyze;
        return (sumSq / elementsAnalyze) - (mean * mean);
    }

    private static void validateNotNull(DoubleRingBuffer doubleRingBuffer) {
        if (doubleRingBuffer == null) {
            throw new IllegalArgumentException("Trading data cannot be null");
        }
    }

    private static void validateSameSize(DoubleRingBuffer... tradingPrices) {
        if (Arrays.stream(tradingPrices).map(DoubleRingBuffer::size).distinct().count() != 1) {
            throw new IllegalArgumentException("Trading prices collections must have the same size");
        }
    }

}
