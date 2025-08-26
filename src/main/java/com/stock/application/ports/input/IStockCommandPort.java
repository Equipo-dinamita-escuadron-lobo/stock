package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

public interface IStockCommandPort {
    Stock save(Stock stock);
    Stock update(Long productId, String name);
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);
}
