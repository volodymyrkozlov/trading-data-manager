package com.volodymyrkozlov.tradingdatamanager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.averageTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.lastTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.minTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.maxTradingPrice;
import static com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataAnalyzer.varianceTradingPrice;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SymbolFinancialDataAnalyzerTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/last_trading_price_sheet.csv", numLinesToSkip = 1)
    void returnsLastTradingPrice(String tradingPricesInput,
                                 Double expected) {
        // given
        var tradingPrices = parseToList(tradingPricesInput);

        // when
        var lastTradingPrice = lastTradingPrice(tradingPrices);

        // then
        assertThat(lastTradingPrice).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/min_trading_price_sheet.csv", numLinesToSkip = 1)
    void returnsMinTradingPrice(String tradingPricesInput,
                                String minDequeInput,
                                int k,
                                Double expected) {
        // given
        var tradingPrices = parseToList(tradingPricesInput);
        var minDeque = Map.of(k, parseToDeque(minDequeInput));

        // when
        var minTradingPrice = minTradingPrice(tradingPrices, minDeque, k);

        // then
        assertThat(minTradingPrice).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/max_trading_price_sheet.csv", numLinesToSkip = 1)
    void returnsMaxTradingPrice(String tradingPricesInput,
                                String maxDequeInput,
                                int k,
                                Double expected) {
        // given
        var tradingPrices = parseToList(tradingPricesInput);
        var maxDeque = Map.of(k, parseToDeque(maxDequeInput));

        // when
        var maxTradingPrice = maxTradingPrice(tradingPrices, maxDeque, k);

        // then
        assertThat(maxTradingPrice).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/avg_trading_price_sheet.csv", numLinesToSkip = 1)
    void returnsAvgTradingPrice(String tradingPricesInput,
                                String tradingPricesPrefixSumsInput,
                                int k,
                                Double expected) {
        // given
        var tradingPrices = parseToList(tradingPricesInput);
        var tradingPricesPrefixSums = parseToList(tradingPricesPrefixSumsInput);

        // when
        var averageTradingPrice = averageTradingPrice(tradingPrices, tradingPricesPrefixSums, k);

        // then
        assertThat(averageTradingPrice).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/var_trading_price_sheet.csv", numLinesToSkip = 1)
    void returnsVarTradingPrice(String tradingPricesInput,
                                String tradingPricesPrefixSumsInput,
                                String tradingPricesPrefixSquaresInput,
                                int k,
                                Double expected) {
        // given
        var tradingPrices = parseToList(tradingPricesInput);
        var tradingPricesPrefixSums = parseToList(tradingPricesPrefixSumsInput);
        var tradingPricesPrefixSquares = parseToList(tradingPricesPrefixSquaresInput);

        // when
        var averageTradingPrice = varianceTradingPrice(tradingPrices, tradingPricesPrefixSums, tradingPricesPrefixSquares, k);

        // then
        assertThat(averageTradingPrice).isEqualTo(expected);
    }

    @Test
    void throwsExceptionIfLastTradingHasEmptyInput() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> lastTradingPrice(List.of()));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices don't contain at least one data");
    }

    @Test
    void throwsExceptionIfAverageInputDataHasDifferentSize() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> averageTradingPrice(List.of(1.0), List.of(1.0, 2.0), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices collections must have the same size");
    }

    @Test
    void throwsExceptionIfAverageInputDataHasEmptyInput() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> averageTradingPrice(List.of(), List.of(), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices don't contain at least one data");
    }

    @Test
    void throwsExceptionIfVarianceInputDataHasDifferentSize() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> varianceTradingPrice(List.of(1.0), List.of(1.0, 2.0), List.of(1.0), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices collections must have the same size");
    }

    @Test
    void throwsExceptionIfVarianceInputDataHasEmptyInput() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> varianceTradingPrice(List.of(), List.of(), List.of(), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices don't contain at least one data");
    }

    @Test
    void throwsExceptionWhenMaxDequeDontContainProvidedKElement() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> maxTradingPrice(List.of(1.0), Map.of(), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Dequeues don't contain provided K 1");
    }

    @Test
    void throwsExceptionWhenMinDequeDontContainProvidedKElement() {
        // given
        var exception = assertThrows(IllegalArgumentException.class, () -> minTradingPrice(List.of(1.0), Map.of(), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Dequeues don't contain provided K 1");
    }

    @Test
    void throwsExceptionWhenMaxDequeIsEmpty() {
        // given
        var maxDeque = new ArrayDeque<Integer>();

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> maxTradingPrice(List.of(1.0), Map.of(1, maxDeque), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Deque for K 1 doesn't contain any data");
    }

    @Test
    void throwsExceptionWhenMinDequeIsEmpty() {
        // given
        var minDeque = new ArrayDeque<Integer>();

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> minTradingPrice(List.of(1.0), Map.of(1, minDeque), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Deque for K 1 doesn't contain any data");
    }

    @Test
    void throwsExceptionWhenTradingPricesDontContainIndexFromMaxDeque() {
        // given
        var maxDeque = new ArrayDeque<Integer>();
        maxDeque.add(2);

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> maxTradingPrice(List.of(1.0), Map.of(1, maxDeque), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices don't contain index 2 from deque");
    }

    @Test
    void throwsExceptionWhenTradingPricesDontContainIndexFromMinDeque() {
        // given
        var minDeque = new ArrayDeque<Integer>();
        minDeque.add(2);

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> minTradingPrice(List.of(1.0), Map.of(1, minDeque), 1));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading prices don't contain index 2 from deque");
    }


    private static Deque<Integer> parseToDeque(String input) {
        return Arrays.stream(input.split(";"))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(toCollection(ArrayDeque::new));
    }

    private static List<Double> parseToList(String input) {
        return Arrays.stream(input.split(";"))
                .map(String::trim)
                .map(Double::parseDouble)
                .toList();
    }
}
