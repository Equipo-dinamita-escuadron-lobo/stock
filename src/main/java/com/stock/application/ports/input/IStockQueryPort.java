package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

/**
 * @Brief Interface for query operations on Stock entities.
 */
public interface IStockQueryPort {
    Stock findByProductId(Long id);

    boolean existsByProductId(Long id);
}
