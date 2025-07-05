package com.stock.application.service;

import org.springframework.stereotype.Service;

import com.stock.application.ports.input.IStockCommandPort;
import com.stock.domain.model.Stock;
import com.stock.domain.port.IFormatterResultOutputPort;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.domain.port.IStockQueryRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockCommandService implements IStockCommandPort{

    private final IStockCommandRepositoryPort stockCommandPort;
    private final IStockQueryRepositoryPort stockQueryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public Stock save(Stock stock) {
        if (stockQueryPort.existsById(stock.getProductId())) {
            formatterResultOutputPort.returnResponseError(409, "El Producto con el id " + stock.getId() + " ya existe.");
        }

        return stockCommandPort.save(stock);
    }

    @Override
    public void inactivate(Long id) {
        if (!stockQueryPort.existsById(id)) {
            formatterResultOutputPort.returnResponseError(404, "El Producto con el id " + id + " no existe.");
        }

        stockCommandPort.inactivate(id);
    }

    @Override
    public void activate(Long id) {
        if (!stockQueryPort.existsById(id)) {
            formatterResultOutputPort.returnResponseError(404, "El Producto con el id " + id + " no existe.");
        }

        stockCommandPort.activate(id);
    }

    @Override
    public void increaseStock(Long id, int amount) {
        if (!stockQueryPort.existsById(id)) {
            formatterResultOutputPort.returnResponseError(404, "El Producto con el id " + id + " no existe.");
        }

        stockCommandPort.increaseStock(id, amount);
    }

    @Override
    public void decreaseStock(Long id, int amount) {
        if (!stockQueryPort.existsById(id)) {
            formatterResultOutputPort.returnResponseError(404, "El Producto con el id " + id + " no existe.");
        }

        stockCommandPort.decreaseStock(id, amount);
    }


    @Override
    public void changePrice(Long id, double newPrice) {
        if (!stockQueryPort.existsById(id)) {
            formatterResultOutputPort.returnResponseError(404, "El Producto con el id " + id + " no existe.");
        }

        stockCommandPort.changePrice(id, newPrice);
    }
    
}
