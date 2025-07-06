package com.volodymyrkozlov.tradingdatamanager.repository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemorySymbolTradingDataRepositoryTest {
    private final InMemorySymbolTradingDataRepository repository = new InMemorySymbolTradingDataRepository(2, 2, 5);

    @Test
    void initiatesTradingData() {
        // given
        repository.addSymbolTradingData("PLN", List.of(1.0));

        // when
        var tradingDataEntity = repository.getTradingData("PLN");

        // then
        assertThat(tradingDataEntity.tradingPrices())
                .satisfies(tradingPrices -> {
                    assertThat(tradingPrices.size()).isEqualTo(1);
                    assertThat(tradingPrices.getByIndex(0)).isEqualTo(1.0);
                });
        assertThat(tradingDataEntity.tradingPricesPrefixSums())
                .satisfies(tradingPricesPrefixSums -> {
                    assertThat(tradingPricesPrefixSums.size()).isEqualTo(1);
                    assertThat(tradingPricesPrefixSums.getByIndex(0)).isEqualTo(1.0);
                });
        assertThat(tradingDataEntity.tradingPricesPrefixSquares())
                .satisfies(tradingPricesPrefixSquares -> {
                    assertThat(tradingPricesPrefixSquares.size()).isEqualTo(1);
                    assertThat(tradingPricesPrefixSquares.getByIndex(0)).isEqualTo(1.0);
                });
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(10, deque -> assertThat(deque).containsExactly(0));
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(100, deque -> assertThat(deque).containsExactly(0));
    }

    @Test
    void addsTradingData() {
        // given
        repository.addSymbolTradingData("PLN", List.of(5.0));
        repository.addSymbolTradingData("PLN", List.of(7.0));
        repository.addSymbolTradingData("PLN", List.of(20.0));
        repository.addSymbolTradingData("PLN", List.of(9.0));
        repository.addSymbolTradingData("PLN", List.of(8.0));

        // when
        var tradingDataEntity = repository.getTradingData("PLN");

        // then
        assertThat(tradingDataEntity.tradingPrices())
                .satisfies(tradingPrices -> {
                    assertThat(tradingPrices.size()).isEqualTo(5);
                    assertThat(tradingPrices.getByIndex(0)).isEqualTo(5.0);
                    assertThat(tradingPrices.getByIndex(1)).isEqualTo(7.0);
                    assertThat(tradingPrices.getByIndex(2)).isEqualTo(20.0);
                    assertThat(tradingPrices.getByIndex(3)).isEqualTo(9.0);
                    assertThat(tradingPrices.getByIndex(4)).isEqualTo(8.0);
                });
        assertThat(tradingDataEntity.tradingPricesPrefixSums())
                .satisfies(tradingPricesPrefixSums -> {
                    assertThat(tradingPricesPrefixSums.size()).isEqualTo(5);
                    assertThat(tradingPricesPrefixSums.getByIndex(0)).isEqualTo(5.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(1)).isEqualTo(12.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(2)).isEqualTo(32.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(3)).isEqualTo(41.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(4)).isEqualTo(49.0);
                });
        assertThat(tradingDataEntity.tradingPricesPrefixSquares())
                .satisfies(tradingPricesPrefixSums -> {
                    assertThat(tradingPricesPrefixSums.size()).isEqualTo(5);
                    assertThat(tradingPricesPrefixSums.getByIndex(0)).isEqualTo(25.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(1)).isEqualTo(74.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(2)).isEqualTo(474.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(3)).isEqualTo(555.0);
                    assertThat(tradingPricesPrefixSums.getByIndex(4)).isEqualTo(619.0);
                });
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(10, deque -> assertThat(deque).containsExactly(2, 3, 4));
        assertThat(tradingDataEntity.maxDequeues()).hasEntrySatisfying(100, deque -> assertThat(deque).containsExactly(2, 3, 4));
        assertThat(tradingDataEntity.minDequeues()).hasEntrySatisfying(10, deque -> assertThat(deque).containsExactly(0, 1, 4));
        assertThat(tradingDataEntity.minDequeues()).hasEntrySatisfying(100, deque -> assertThat(deque).containsExactly(0, 1, 4));
    }

    @Test
    void throwsExceptionIfSymbolsLimitIsReached() {
        // given
        repository.addSymbolTradingData("PLN", List.of(5.0));
        repository.addSymbolTradingData("UAH", List.of(7.0));

        // when
        var exception = assertThrows(IllegalStateException.class, () -> repository.addSymbolTradingData("USD", List.of(7.0)));

        // then
        assertThat(exception.getMessage()).isEqualTo("Trading data symbol limit of 2 is reached");
    }

    @Test
    void throwsExceptionIfBatchSizeExceeded() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> repository.addSymbolTradingData("PLN", List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));

        // then
        assertThat(exception.getMessage()).isEqualTo("Batch size 6 is greater than allowed 5");
    }
}
