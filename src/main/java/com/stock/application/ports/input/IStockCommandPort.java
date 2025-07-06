package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

public interface IStockCommandPort {
    Stock commercialInput(Stock stock);
    Stock commercialOutput(Stock stock);

    void inactivate(Long id);
    void activate(Long id);
}
