package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockCommandRepositoryPort {
    Stock save(Stock stock);
    Stock update(Long productId, String name);
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);
}
