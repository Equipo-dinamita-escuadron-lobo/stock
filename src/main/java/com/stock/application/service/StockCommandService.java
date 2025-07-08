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
    public Stock registerSale(Stock stock) {
        Stock oldStock = stockQueryPort.findById(stock.getId());
        // The Sell method will throw an exception if the stock is not active or if the amount is invalid.
        oldStock.sell(stock.getQuantity());
        return stockCommandPort.registerSale(oldStock);
    }

    @Override
    public Stock registerPurchase(Stock stock) {
        Stock oldStock = stockQueryPort.findById(stock.getId());
        // The Buy method will throw an exception if the stock is not active or if the amount or price is invalid.
        oldStock.buy(stock.getQuantity(), stock.getPrice());
        return stockCommandPort.registerPurchase(oldStock);
    }
    
}
