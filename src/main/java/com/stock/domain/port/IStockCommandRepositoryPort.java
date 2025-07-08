package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockCommandRepositoryPort {
    Stock save(Stock stock);
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);

    void inactivate(Long id);
    void activate(Long id);
}
