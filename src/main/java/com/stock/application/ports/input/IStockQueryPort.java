package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

public interface IStockQueryPort {
    Stock findByProductId(Long id);

    boolean existsByProductId(Long id);
}
