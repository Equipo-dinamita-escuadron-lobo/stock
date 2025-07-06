package com.stock.application.ports.input;

import com.stock.domain.model.Stock;

public interface IStockQueryPort {
    Stock findById(Long id);

    boolean existsById(Long id);
}
