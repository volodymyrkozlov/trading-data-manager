package com.volodymyrkozlov.tradingdatamanager.repository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemorySymbolTradingDataRepositoryTest {
    private final InMemorySymbolTradingDataRepository repository = new InMemorySymbolTradingDataRepository(1, 2, 5);

    @Test
    void initiatesTradingData() {
        // given
        repository.addSymbolTradingData("PLN", List.of(1.0));

        // when
        var tradingDataEntity = repository.getTradingData("PLN");

        // then
        assertThat(tradingDataEntity.tradingPrices()).isEqualTo(List.of(1.0));
        assertThat(tradingDataEntity.tradingPricesPrefixSums()).isEqualTo(List.of(1.0));
        assertThat(tradingDataEntity.tradingPricesPrefixSquares()).isEqualTo(List.of(1.0));
    }

    @Test
    void addsTradingData() {
        // given
        repository.addSymbolTradingData("PLN", List.of(5.0));
        repository.addSymbolTradingData("PLN",  List.of(7.0));
        repository.addSymbolTradingData("PLN",  List.of(20.0));
        repository.addSymbolTradingData("PLN",  List.of(9.0));
        repository.addSymbolTradingData("PLN",  List.of(8.0));

        // when
        var tradingDataEntity = repository.getTradingData("PLN");

        // then
        assertThat(tradingDataEntity.tradingPrices()).isEqualTo(List.of(5.0, 7.0, 20.0, 9.0, 8.0));
        assertThat(tradingDataEntity.tradingPricesPrefixSums()).isEqualTo(List.of(5.0, 12.0, 32.0, 41.0, 49.0));
        assertThat(tradingDataEntity.tradingPricesPrefixSquares()).isEqualTo(List.of(25.0, 74.0, 474.0, 555.0, 619.0));
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(10, deque -> assertThat(deque).containsExactly(2, 3,4));
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(100, deque -> assertThat(deque).containsExactly(2, 3,4));
        assertThat(tradingDataEntity.minDequeues()).hasEntrySatisfying(10, deque -> assertThat(deque).containsExactly(0, 1,4));
        assertThat(tradingDataEntity.minDequeues()).hasEntrySatisfying(100, deque -> assertThat(deque).containsExactly(0, 1,4));
    }

    @Test
    void throwsExceptionIfSymbolsLimitIsReached() {
        // given
        repository.addSymbolTradingData("PLN",  List.of(5.0));
        repository.addSymbolTradingData("UAH", List.of(7.0));

        // when
        var exception = assertThrows(IllegalStateException.class, () -> repository.addSymbolTradingData("UAH", List.of(7.0)));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading data symbol limit reached");
    }

    @Test
    void throwsExceptionIfBatchSizeExceeded() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> repository.addSymbolTradingData("PLN", List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));

        // then
        assertThat(exception.getMessage()).isEqualTo("Batch size 6 is greater than allowed 5");
    }
}
