package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

public interface IStockCommandPort {
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);

    void inactivate(Long id);
    void activate(Long id);
}
