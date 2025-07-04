package com.volodymyrkozlov.tradingdatamanager.controller;

import com.volodymyrkozlov.tradingdatamanager.dto.FinancialDataBatchRequest;
import com.volodymyrkozlov.tradingdatamanager.dto.FinancialDataResponse;
import com.volodymyrkozlov.tradingdatamanager.service.SymbolFinancialDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinancialDataController {
    private final SymbolFinancialDataService symbolFinancialDataService;

    public FinancialDataController(SymbolFinancialDataService symbolFinancialDataService) {
        this.symbolFinancialDataService = symbolFinancialDataService;
    }

    @PostMapping("/add_batch")
    public void addBatch(@RequestBody FinancialDataBatchRequest request) {
        symbolFinancialDataService.addFinancialData(request.symbol(), request.values());
    }

    @GetMapping("/stats/{symbol}/{k}")
    public FinancialDataResponse getStats(@PathVariable("symbol") String symbol,
                                          @PathVariable("k") int k) {
        return symbolFinancialDataService.getFinancialData(symbol, k);
    }
}
