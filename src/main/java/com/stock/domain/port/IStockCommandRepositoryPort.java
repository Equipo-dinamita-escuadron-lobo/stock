package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockCommandRepositoryPort {
    Stock save(Stock stock);
    Stock commercialInput(Stock stock);
    Stock commercialOutput(Stock stock);

    void inactivate(Long id);
    void activate(Long id);
}
