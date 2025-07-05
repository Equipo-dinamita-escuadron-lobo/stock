package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockCommandRepositoryPort {
    Stock save(Stock stock);

    void inactivate(Long id);
    void activate(Long id);

    void increaseStock(Long id, int amount);
    void decreaseStock(Long id, int amount);
    void changePrice(Long id, double newPrice);
}
