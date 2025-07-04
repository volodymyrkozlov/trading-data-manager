package com.volodymyrkozlov.tradingdatamanager.repository;

public interface SymbolTradingDataRepository {

    void addSymbolTradingData(String symbol,
                              Double price);

    TradingDataEntity getTradingData(String symbol);
}
