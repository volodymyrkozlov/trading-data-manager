package com.volodymyrkozlov.tradingdatamanager.service;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.min;

class SymbolFinancialDataAnalyzer {

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    static double lastTradingPrice(List<Double> tradingPrices) {
        validateNotEmpty(tradingPrices);

        return tradingPrices.getLast();
    }

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    static double averageTradingPrice(List<Double> tradingPrices,
                                      List<Double> tradingPricesPrefixSums,
                                      int k) {
        validateNotEmpty(tradingPrices, tradingPricesPrefixSums);
        validateSameSize(tradingPrices, tradingPricesPrefixSums);
        final var size = tradingPricesPrefixSums.size();
        final var elementsAnalyze = min(tradingPricesPrefixSums.size(), k);

        final var lastIndex = size - 1;
        final var firstIndex = size - elementsAnalyze;

        final var total = tradingPricesPrefixSums.get(lastIndex);
        final var excluded = (firstIndex > 0) ? tradingPricesPrefixSums.get(firstIndex - 1) : 0.0;

        return (total - excluded) / elementsAnalyze;
    }

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    static double varianceTradingPrice(List<Double> tradingPrices,
                                       List<Double> tradingPricesPrefixSums,
                                       List<Double> tradingPricesPrefixSquares,
                                       int k) {
        validateNotEmpty(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares);
        validateSameSize(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares);
        final var size = tradingPrices.size();
        final var elementsAnalyze = min(tradingPrices.size(), k);

        if (elementsAnalyze == 1) {
            return 0.0;
        }

        var sum = tradingPricesPrefixSums.get(size - 1);
        var sumSq = tradingPricesPrefixSquares.get(size - 1);

        if (size - elementsAnalyze - 1 >= 0) {
            sum -= tradingPricesPrefixSums.get(size - elementsAnalyze - 1);
            sumSq -= tradingPricesPrefixSquares.get(size - elementsAnalyze - 1);
        }

        return elementsAnalyze == tradingPrices.size()
                ? populationVariance(sumSq, sum, elementsAnalyze)
                : sampleVariance(sumSq, sum, elementsAnalyze);
    }

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    static double minTradingPrice(List<Double> tradingPrices,
                                  Map<Integer, Deque<Integer>> minDequeues,
                                  int k) {
        return resolveDequeStats(tradingPrices, minDequeues, k);
    }

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    static double maxTradingPrice(List<Double> tradingPrices,
                                  Map<Integer, Deque<Integer>> maxDequeues,
                                  int k) {
        return resolveDequeStats(tradingPrices, maxDequeues, k);
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


    private static double resolveDequeStats(List<Double> tradingPrices,
                                            Map<Integer, Deque<Integer>> dequeues,
                                            int k) {
        validateNotEmpty(tradingPrices);
        final var deque = Optional.ofNullable(dequeues.get(k))
                .orElseThrow(() -> new IllegalArgumentException("Dequeues don't contain provided K %s".formatted(k)));

        if (deque.isEmpty()) {
            throw new IllegalArgumentException("Deque for K %s doesn't contain any data".formatted(k));
        }

        final var index = deque.peekFirst();

        try {
            return tradingPrices.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Trading prices don't contain index %s from deque".formatted(index));
        }
    }

    private static void validateNotEmpty(List<Double>... tradingPrices) {
        if (Arrays.stream(tradingPrices).anyMatch(collection -> collection == null || collection.isEmpty())) {
            throw new IllegalArgumentException("Trading prices don't contain at least one data");
        }
    }

    private static void validateSameSize(List<Double>... tradingPrices) {
        if (Arrays.stream(tradingPrices).map(List::size).distinct().count() != 1) {
            throw new IllegalArgumentException("Trading prices collections must have the same size");
        }
    }

}
