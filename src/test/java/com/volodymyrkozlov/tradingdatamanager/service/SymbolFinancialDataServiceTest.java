package com.volodymyrkozlov.tradingdatamanager.service;

import com.volodymyrkozlov.tradingdatamanager.repository.DoubleRingBuffer;
import com.volodymyrkozlov.tradingdatamanager.repository.SymbolTradingDataRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import static com.volodymyrkozlov.tradingdatamanager.repository.TradingDataEntity.tradingDataEntityBuilder;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SymbolFinancialDataServiceTest {
    private final SymbolTradingDataRepository symbolTradingDataRepository = mock(SymbolTradingDataRepository.class);
    private final SymbolFinancialDataService symbolFinancialDataService =
            new SymbolFinancialDataService(symbolTradingDataRepository, 8);

    @Test
    void addsSymbolTradingData() {
        // when
        symbolFinancialDataService.addFinancialData("PLN", List.of(1.0, 2.0, 3.0));

        // then
        verify(symbolTradingDataRepository).addSymbolTradingData("PLN", List.of(1.0, 2.0, 3.0));
    }

    @Test
    void returnsFinancialData() {
        // given
        var maxDeque = new ArrayDeque<Integer>();
        maxDeque.add(0);
        var minDeque = new ArrayDeque<Integer>();
        minDeque.add(0);

        var maxDeques = new HashMap<Integer, Deque<Integer>>();
        maxDeques.put(10, maxDeque);
        var minDeques = new HashMap<Integer, Deque<Integer>>();
        minDeques.put(10, minDeque);

        var tradingPrices = new DoubleRingBuffer(1);
        tradingPrices.add(1);
        var tradingPricesPrefixSums = new DoubleRingBuffer(1);
        tradingPricesPrefixSums.add(1);
        var tradingPricesPrefixSquares = new DoubleRingBuffer(1);
        tradingPricesPrefixSquares.add(1);

        var tradingDataEntity = tradingDataEntityBuilder()
                .tradingPrices(tradingPrices)
                .tradingPricesPrefixSums(tradingPricesPrefixSums)
                .tradingPricesPrefixSquares(tradingPricesPrefixSquares)
                .maxDequeues(maxDeques)
                .minDequeues(minDeques)
                .build();

        when(symbolTradingDataRepository.getTradingData("PLN")).thenReturn(tradingDataEntity);

        // when
        var response = symbolFinancialDataService.getFinancialData("PLN", 1);

        // then
        assertThat(response.min()).isEqualTo(1.0);
        assertThat(response.max()).isEqualTo(1.0);
        assertThat(response.last()).isEqualTo(1.0);
        assertThat(response.avg()).isEqualTo(1.0);
        assertThat(response.var()).isEqualTo(0.0);
    }

    @Test
    void throwsExceptionIfMaxKValueExceeded() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> symbolFinancialDataService.getFinancialData("PLN", 9));

        // then
        assertThat(exception.getMessage()).isEqualTo("K value 9 is greater than allowed 8");
    }
}
