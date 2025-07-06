package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockQueryRepositoryPort {
    Stock findById(Long id);

    boolean existsById(Long id);
}
