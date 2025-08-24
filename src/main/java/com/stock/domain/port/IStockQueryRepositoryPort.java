package com.stock.domain.port;

import com.stock.domain.model.Stock;

public interface IStockQueryRepositoryPort {
    Stock findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
