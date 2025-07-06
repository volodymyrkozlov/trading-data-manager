package com.volodymyrkozlov.tradingdatamanager.repository;

import java.util.List;

public interface SymbolTradingDataRepository {

    void addSymbolTradingData(String symbol,
                              List<Double> prices);

    TradingDataEntity getTradingData(String symbol);
}
