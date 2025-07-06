package com.volodymyrkozlov.tradingdatamanager.repository;

import com.volodymyrkozlov.tradingdatamanager.utils.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.volodymyrkozlov.tradingdatamanager.repository.TradingDataEntity.tradingDataEntityBuilder;
import static java.util.stream.IntStream.rangeClosed;

@Repository
public class InMemorySymbolTradingDataRepository implements SymbolTradingDataRepository {
    private final Map<String, TradingDataEntity> symbolTradingPriceData = new ConcurrentHashMap<>();

    private final Integer symbolsAllowedAmount;
    private final Integer maxKValue;
    private final Integer maxBatchSize;


    public InMemorySymbolTradingDataRepository(@Value("${symbols-allowed-amount}") Integer symbolsAllowedAmount,
                                               @Value("${max-k-value}") Integer maxKValue,
                                               @Value("${max-batch-size}") Integer maxBatchSize) {
        this.symbolsAllowedAmount = symbolsAllowedAmount;
        this.maxKValue = maxKValue;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public void addSymbolTradingData(String symbol,
                                     List<Double> prices) {
        validateMaxSymbolAllowed();
        validateMaxBatchSize(prices);

        Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .ifPresentOrElse(
                        tradingData -> updateSymbolTradingPriceData(prices, tradingData),
                        () -> initSymbolTradingPriceData(symbol, prices));
    }

    @Override
    public TradingDataEntity getTradingData(String symbol) {
        return Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .orElseThrow(() -> new EntityNotFoundException("Trading price data is not found for %s".formatted(symbol)));
    }

    private void initSymbolTradingPriceData(String symbol,
                                            List<Double> prices) {
        final var tradingPrices = new ArrayList<Double>();
        final var tradingPricesPrefixSums = new ArrayList<Double>();
        final var tradingPricesPrefixSquares = new ArrayList<Double>();
        final var maxDequeues = new HashMap<Integer, Deque<Integer>>();
        final var minDequeues = new HashMap<Integer, Deque<Integer>>();

        addTradingPricingData(prices, tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, 0.0, 0.0);

        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var maxDeque = new ArrayDeque<Integer>();
                    final var minDeque = new ArrayDeque<Integer>();

                    for (var i = 0; i < prices.size(); i++) {
                        initDeque(maxDeque, prices, i, k, true);
                        initDeque(minDeque, prices, i, k, false);
                    }

                    maxDequeues.put(k, maxDeque);
                    minDequeues.put(k, minDeque);
                });

        symbolTradingPriceData.put(symbol, tradingDataEntityBuilder()
                .tradingPrices(tradingPrices)
                .tradingPricesPrefixSums(tradingPricesPrefixSums)
                .tradingPricesPrefixSquares(tradingPricesPrefixSquares)
                .maxDequeues(maxDequeues)
                .minDequeues(minDequeues)
                .build());
    }

    private void updateSymbolTradingPriceData(List<Double> prices,
                                              TradingDataEntity tradingData) {
        final var tradingPrices = tradingData.tradingPrices();
        final var tradingPricesPrefixSums = tradingData.tradingPricesPrefixSums();
        final var tradingPricesPrefixSquares = tradingData.tradingPricesPrefixSquares();

        final var startIndex = tradingPrices.size();

        var lastSum = tradingPricesPrefixSums.getLast();
        var lastSqSum = tradingPricesPrefixSquares.getLast();
        addTradingPricingData(prices, tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, lastSum, lastSqSum);

        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var maxDeque = tradingData.maxDequeues().get(k);
                    final var minDeque = tradingData.minDequeues().get(k);

                    for (var i = 0; i < prices.size(); i++) {
                        final var index = startIndex + i;
                        updateDeque(maxDeque, tradingPrices, prices.get(i), index, k, true);
                        updateDeque(minDeque, tradingPrices, prices.get(i), index, k, false);
                    }
                });
    }

    private static void addTradingPricingData(List<Double> prices,
                                              List<Double> tradingPrices,
                                              List<Double> tradingPricesPrefixSums,
                                              List<Double> tradingPricesPrefixSquares,
                                              double sum,
                                              double sumSq) {
        for (final var price : prices) {
            tradingPrices.add(price);
            sum += price;
            sumSq += price * price;
            tradingPricesPrefixSums.add(sum);
            tradingPricesPrefixSquares.add(sumSq);
        }
    }

    private static void initDeque(Deque<Integer> deque,
                                  List<Double> prices,
                                  int currentIndex,
                                  int k,
                                  boolean isMax) {
        while (!deque.isEmpty() && deque.peekFirst() <= currentIndex - k) {
            deque.pollFirst();
        }

        while (!deque.isEmpty() &&
                ((isMax && prices.get(deque.peekLast()) <= prices.get(currentIndex)) ||
                        (!isMax && prices.get(deque.peekLast()) >= prices.get(currentIndex)))) {
            deque.pollLast();
        }

        deque.addLast(currentIndex);
    }

    private static void updateDeque(Deque<Integer> deque,
                                    List<Double> prices,
                                    double price,
                                    int currentIndex,
                                    int k,
                                    boolean isMax) {
        while (!deque.isEmpty() && deque.peekFirst() <= currentIndex - k) {
            deque.pollFirst();
        }

        while (!deque.isEmpty()
                && ((isMax && prices.get(deque.peekLast()) <= price) || (!isMax && prices.get(deque.peekLast()) >= price))) {
            deque.pollLast();
        }

        deque.addLast(currentIndex);
    }

    private void validateMaxSymbolAllowed() {
        if (symbolTradingPriceData.size() > symbolsAllowedAmount) {
            throw new IllegalStateException("Trading data symbol limit reached");
        }
    }

    private void validateMaxBatchSize(Collection<Double> values) {
        if (values.size() > maxBatchSize) {
            throw new IllegalArgumentException("Batch size %s is greater than allowed %s".formatted(values.size(), maxBatchSize));
        }
    }
}
