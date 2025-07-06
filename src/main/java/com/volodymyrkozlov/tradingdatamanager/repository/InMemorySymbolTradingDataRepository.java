package com.volodymyrkozlov.tradingdatamanager.repository;

import com.volodymyrkozlov.tradingdatamanager.utils.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.volodymyrkozlov.tradingdatamanager.repository.TradingDataEntity.tradingDataEntityBuilder;
import static com.volodymyrkozlov.tradingdatamanager.utils.MathUtils.powerOfTen;
import static java.util.stream.IntStream.of;
import static java.util.stream.IntStream.rangeClosed;

@Repository
public class InMemorySymbolTradingDataRepository implements SymbolTradingDataRepository {
    private final Map<String, TradingDataEntity> symbolTradingPriceData = new ConcurrentHashMap<>();

    private final Integer symbolsAllowedAmount;
    private final Integer maxKValue;
    private final Integer maxBatchSize;
    private final int maxSymbolTradingDataCapacity;

    public InMemorySymbolTradingDataRepository(@Value("${symbols-allowed-amount}") Integer symbolsAllowedAmount,
                                               @Value("${max-k-value}") Integer maxKValue,
                                               @Value("${max-batch-size}") Integer maxBatchSize) {
        this.symbolsAllowedAmount = symbolsAllowedAmount;
        this.maxKValue = maxKValue;
        this.maxBatchSize = maxBatchSize;
        this.maxSymbolTradingDataCapacity = powerOfTen(maxKValue);
    }

    @Override
    public void addSymbolTradingData(String symbol,
                                     List<Double> prices) {
        validateMaxSymbolAllowed();
        validateMaxBatchSize(prices);

        symbolTradingPriceData.compute(symbol, (key, entity) -> {
            if (entity == null) {
                return initSymbolTradingPriceData(symbol, prices);
            } else {
                synchronized (entity) {
                    updateSymbolTradingPriceData(prices, entity);
                }
                return entity;
            }
        });
    }

    @Override
    public TradingDataEntity getTradingData(String symbol) {
        return Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .orElseThrow(() -> new EntityNotFoundException("Trading price data is not found for %s".formatted(symbol)));
    }

    private TradingDataEntity initSymbolTradingPriceData(String symbol,
                                                         List<Double> prices) {
        final var tradingPrices = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var tradingPricesPrefixSums = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var tradingPricesPrefixSquares = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var maxDequeues = new HashMap<Integer, Deque<Integer>>();
        final var minDequeues = new HashMap<Integer, Deque<Integer>>();

        addTradingData(prices, tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, 0.0, 0.0);

        final var lastIndex = tradingPrices.currentIndex();
        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var maxDeque = new ArrayDeque<Integer>();
                    final var minDeque = new ArrayDeque<Integer>();

                    for (var i = lastIndex - prices.size() + 1; i <= lastIndex; i++) {
                        updateDeque(maxDeque, tradingPrices, i, k, true);
                        updateDeque(minDeque, tradingPrices, i, k, false);
                    }

                    maxDequeues.put(k, maxDeque);
                    minDequeues.put(k, minDeque);
                });

        return tradingDataEntityBuilder()
                .tradingPrices(tradingPrices)
                .tradingPricesPrefixSums(tradingPricesPrefixSums)
                .tradingPricesPrefixSquares(tradingPricesPrefixSquares)
                .maxDequeues(maxDequeues)
                .minDequeues(minDequeues)
                .build();
    }

    private void updateSymbolTradingPriceData(List<Double> prices,
                                              TradingDataEntity tradingData) {
        final var tradingPrices = tradingData.tradingPrices();
        final var prefixSums = tradingData.tradingPricesPrefixSums();
        final var prefixSquares = tradingData.tradingPricesPrefixSquares();

        final var sum = prefixSums.getByIndex(prefixSums.currentIndex());
        final var sumSq = prefixSquares.getByIndex(prefixSquares.currentIndex());

        addTradingData(prices, tradingPrices, prefixSums, prefixSquares, sum, sumSq);

        final var lastIndex = tradingPrices.currentIndex();

        rangeClosed(1, maxKValue).map(MathUtils::powerOfTen).forEach(k -> {
            final var maxDeque = tradingData.maxDequeues().get(k);
            final var minDeque = tradingData.minDequeues().get(k);

            for (var i = lastIndex - prices.size() + 1; i <= lastIndex; i++) {
                updateDeque(maxDeque, tradingPrices, i, k, true);
                updateDeque(minDeque, tradingPrices, i, k, false);
            }
        });
    }

    private static void addTradingData(List<Double> prices,
                                       DoubleRingBuffer tradingPrices,
                                       DoubleRingBuffer prefixSums,
                                       DoubleRingBuffer prefixSquares,
                                       double sum,
                                       double sumSq) {
        for (final var price : prices) {
            sum += price;
            sumSq += price * price;
            tradingPrices.add(price);
            prefixSums.add(sum);
            prefixSquares.add(sumSq);
        }
    }

    private static void updateDeque(Deque<Integer> deque,
                                    DoubleRingBuffer prices,
                                    int currentIndex,
                                    int k,
                                    boolean isMax) {
        while (!deque.isEmpty() && deque.peekFirst() <= currentIndex - k) {
            deque.pollFirst();
        }

        final var value = prices.getByIndex(currentIndex);
        while (!deque.isEmpty()) {
            final var last = deque.peekLast();
            final var lastVal = prices.getByIndex(last);

            if ((isMax && lastVal <= value) || (!isMax && lastVal >= value)) {
                deque.pollLast();
            } else {
                break;
            }
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
