package com.volodymyrkozlov.tradingdatamanager.repository;

import com.volodymyrkozlov.tradingdatamanager.utils.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayDeque;
import java.util.ArrayList;
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

    public InMemorySymbolTradingDataRepository(@Value("${symbols-allowed-amount}") Integer symbolsAllowedAmount,
                                               @Value("${max-k-value}") Integer maxKValue) {
        this.symbolsAllowedAmount = symbolsAllowedAmount;
        this.maxKValue = maxKValue;
    }

    /**
     * Time complexity is O(k) for both initialization and update, where the maximum value of k is 8. Since 8 is a small and fixed number, this can be treated as O(1).
     * Space complexity is for initialization O(k) where the maximum value of k is 8. Since 8 is a small and fixed number, this can be treated as O(1).
     * Space complexity is O(1) for update.
     */
    @Override
    public void addSymbolTradingData(String symbol,
                                     Double price) {
        if (symbolTradingPriceData.size() > symbolsAllowedAmount) {
            throw new IllegalStateException("Trading data symbol limit reached");
        }

        Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .ifPresentOrElse(
                        tradingData -> updateSymbolTradingPriceData(price, tradingData),
                        () -> initSymbolTradingPriceData(symbol, price));
    }

    /**
     * Time complexity is O(1).
     * Space complexity is O(1).
     */
    @Override
    public TradingDataEntity getTradingData(String symbol) {
        return Optional.ofNullable(symbolTradingPriceData.get(symbol))
                .orElseThrow(() -> new EntityNotFoundException("Trading price data is not found for %s".formatted(symbol)));
    }

    /**
     * Time complexity is O(k) where the maximum value of k is 8. Since 8 is a small and fixed number, this can be treated as O(1).
     * Space complexity is O(k) where the maximum value of k is 8. Since 8 is a small and fixed number, this can be treated as O(1).
     */
    private void initSymbolTradingPriceData(String symbol,
                                            Double price) {
        final var tradingPrices = new ArrayList<Double>();
        final var tradingPricesPrefixSums = new ArrayList<Double>();
        final var tradingPricesPrefixSquares = new ArrayList<Double>();
        final var maxDequeues = new HashMap<Integer, Deque<Integer>>();
        final var minDequeues = new HashMap<Integer, Deque<Integer>>();

        tradingPrices.add(price);
        tradingPricesPrefixSums.add(price);
        tradingPricesPrefixSquares.add(price * price);

        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var indexMaxDeque = new ArrayDeque<Integer>();
                    final var indexMinDeque = new ArrayDeque<Integer>();

                    indexMaxDeque.add(0);
                    indexMinDeque.add(0);

                    maxDequeues.put(k, indexMaxDeque);
                    minDequeues.put(k, indexMinDeque);
                });

        symbolTradingPriceData.put(symbol, tradingDataEntityBuilder()
                .tradingPrices(tradingPrices)
                .tradingPricesPrefixSums(tradingPricesPrefixSums)
                .tradingPricesPrefixSquares(tradingPricesPrefixSquares)
                .maxDeque(maxDequeues)
                .minDeque(minDequeues)
                .build());
    }

    /**
     * Time complexity is O(k) where the maximum value of k is 8. Since 8 is a small and fixed number, this can be treated as O(1).
     * Space complexity is O(1).
     */
    private void updateSymbolTradingPriceData(Double price,
                                              TradingDataEntity tradingData) {
        final var tradingPrices = tradingData.tradingPrices();
        final var index = tradingPrices.size();
        tradingPrices.add(price);

        final var tradingPricesPrefixSums = tradingData.tradingPricesPrefixSums();
        tradingPricesPrefixSums.add(tradingPricesPrefixSums.getLast() + price);

        final var tradingPricesPrefixSquares = tradingData.tradingPricesPrefixSquares();
        tradingPricesPrefixSquares.add(tradingPricesPrefixSquares.getLast() + price * price);

        rangeClosed(1, maxKValue)
                .map(MathUtils::powerOfTen)
                .forEach(k -> {
                    final var maxDeque = tradingData.maxDequeues().get(k);
                    final var minDeque = tradingData.minDequeues().get(k);

                    updateDeque(maxDeque, tradingPrices, price, index, k, true);
                    updateDeque(minDeque, tradingPrices, price, index, k, false);
                });
    }

    private static void updateDeque(Deque<Integer> deque,
                                    List<Double> prices,
                                    double price,
                                    int index,
                                    int k,
                                    boolean isMax) {
        while (!deque.isEmpty() && deque.peekFirst() <= index - k) {
            deque.pollFirst();
        }

        while (!deque.isEmpty()
                && ((isMax && prices.get(deque.peekLast()) <= price) || (!isMax && prices.get(deque.peekLast()) >= price))) {
            deque.pollLast();
        }

        deque.addLast(index);
    }
}
