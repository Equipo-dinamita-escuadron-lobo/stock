package com.stock.application.service;

import org.springframework.stereotype.Service;

import com.stock.application.ports.input.IStockQueryPort;
import com.stock.domain.model.Stock;
import com.stock.domain.port.IFormatterResultOutputPort;
import com.stock.domain.port.IStockQueryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockQueryService implements IStockQueryPort {

    private final IStockQueryRepositoryPort stockQueryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public Stock findByProductId(Long id) {
        log.info("Buscando stock con id: {}", id);
        Stock stock = stockQueryPort.findByProductId(id);
        if (stock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + id + " no existe.");
        }
        return stock;
    }

    @Override
    public boolean existsByProductId(Long id) {
        boolean exists = stockQueryPort.existsByProductId(id);
        return exists;
    }
}
