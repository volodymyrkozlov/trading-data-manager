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
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.volodymyrkozlov.tradingdatamanager.repository.TradingDataEntity.tradingDataEntityBuilder;
import static com.volodymyrkozlov.tradingdatamanager.utils.MathUtils.powerOfTen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.IntStream.of;
import static java.util.stream.IntStream.rangeClosed;

@Repository
public class InMemorySymbolTradingDataRepository implements SymbolTradingDataRepository {
    private final Map<String, TradingDataEntity> symbolTradingPriceData = new ConcurrentHashMap<>();
    private final Deque<TradingDataEntity> tradingDataPool;
    private final int maxSymbolsAllowedAmount;
    private final int maxKValue;
    private final int maxBatchSize;
    private final int maxSymbolTradingDataCapacity;

    public InMemorySymbolTradingDataRepository(@Value("${symbols-allowed-amount}") int maxSymbolsAllowedAmount,
                                               @Value("${max-k-value}") int maxKValue,
                                               @Value("${max-batch-size}") int maxBatchSize) {
        this.maxKValue = maxKValue;
        this.maxBatchSize = maxBatchSize;
        this.maxSymbolsAllowedAmount = maxSymbolsAllowedAmount;
        this.maxSymbolTradingDataCapacity = powerOfTen(maxKValue);
        this.tradingDataPool = rangeClosed(1, maxSymbolsAllowedAmount)
                .mapToObj(__ -> initTradingData())
                .collect(toCollection(ConcurrentLinkedDeque::new));
    }

    @Override
    public void addSymbolTradingData(String symbol,
                                     List<Double> prices) {
        validateMaxBatchSize(prices);

        symbolTradingPriceData.compute(symbol, (key, tradingDataEntity) -> {
            if (tradingDataEntity != null) {
                synchronized (tradingDataEntity) {
                    updateSymbolTradingPriceData(prices, tradingDataEntity);
                }
                return tradingDataEntity;
            }

            final var emptyTradingData = tradingDataPool.poll();
            if (emptyTradingData == null) {
                throw new IllegalStateException("Trading data symbol limit of %s is reached".formatted(maxSymbolsAllowedAmount));
            }

            synchronized (emptyTradingData) {
                updateSymbolTradingPriceData(prices, emptyTradingData);
            }

            return emptyTradingData;
        });
    }

    @Override
    public TradingDataEntity getTradingData(String symbol) {
        return Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .orElseThrow(() -> new EntityNotFoundException("Trading price data is not found for %s".formatted(symbol)));
    }

    private void updateSymbolTradingPriceData(List<Double> prices,
                                              TradingDataEntity tradingData) {
        final var tradingPrices = tradingData.tradingPrices();
        final var prefixSums = tradingData.tradingPricesPrefixSums();
        final var prefixSquares = tradingData.tradingPricesPrefixSquares();

        final var sum = prefixSums.size() > 0 ? prefixSums.getByIndex(prefixSums.currentIndex()) : 0.0;
        final var sumSq = prefixSums.size() > 0 ? prefixSquares.getByIndex(prefixSquares.currentIndex()) : 0.0;

        addTradingData(prices, tradingPrices, prefixSums, prefixSquares, sum, sumSq);

        final var lastIndex = tradingPrices.currentIndex();

        rangeClosed(1, maxKValue).map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var maxDeque = tradingData.maxDequeues().get(k);
                    final var minDeque = tradingData.minDequeues().get(k);

                    for (var i = lastIndex - prices.size() + 1; i <= lastIndex; i++) {
                        updateDeque(maxDeque, tradingPrices, i, k, true);
                        updateDeque(minDeque, tradingPrices, i, k, false);
                    }
                });
    }

    private TradingDataEntity initTradingData() {
        final var tradingPrices = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var tradingPricesPrefixSums = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var tradingPricesPrefixSquares = new DoubleRingBuffer(maxSymbolTradingDataCapacity);
        final var maxDequeues = new HashMap<Integer, Deque<Integer>>();
        final var minDequeues = new HashMap<Integer, Deque<Integer>>();

        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    maxDequeues.put(k, new ArrayDeque<>());
                    minDequeues.put(k, new ArrayDeque<>());
                });

        return tradingDataEntityBuilder()
                .tradingPrices(tradingPrices)
                .tradingPricesPrefixSums(tradingPricesPrefixSums)
                .tradingPricesPrefixSquares(tradingPricesPrefixSquares)
                .maxDequeues(maxDequeues)
                .minDequeues(minDequeues)
                .build();
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

    private void validateMaxBatchSize(Collection<Double> values) {
        if (values.size() > maxBatchSize) {
            throw new IllegalArgumentException("Batch size %s is greater than allowed %s".formatted(values.size(), maxBatchSize));
        }
    }
}
