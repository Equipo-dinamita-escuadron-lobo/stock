package com.stock.application.service;

import org.springframework.stereotype.Service;

import com.stock.application.ports.input.IStockCommandPort;
import com.stock.application.ports.input.IStockStatusPort;
import com.stock.domain.model.Stock;
import com.stock.domain.port.IFormatterResultOutputPort;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.domain.port.IStockQueryRepositoryPort;
import com.stock.domain.port.IStockStatusRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCommandService implements IStockCommandPort, IStockStatusPort {

    private final IStockCommandRepositoryPort stockCommandPort;
    private final IStockQueryRepositoryPort stockQueryPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IStockStatusRepositoryPort stockStatusPort;


    @Override
    public void inactivate(Long productId) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + productId + " no existe.");
        }

        stockStatusPort.inactivate(productId);
    }

    @Override
    public void activate(Long productId) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + productId + " no existe.");
        }

        stockStatusPort.activate(productId);
    }

    @Override
    public Stock registerSale(Stock stock) {
        Stock oldStock = stockQueryPort.findByProductId(stock.getProductId());
        if(oldStock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + stock.getProductId() + " no existe.");
        }
        // The Sell method will throw an exception if the stock is not active or if the amount is invalid.
        oldStock.sell(stock.getQuantity());
        log.info("Stock after sale: {}", oldStock);
        return stockCommandPort.registerSale(oldStock);
    }

    @Override
    public Stock registerPurchase(Stock stock) {
        Stock oldStock = stockQueryPort.findByProductId(stock.getProductId());
        if(oldStock == null) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + stock.getProductId() + " no existe.");
        }
        // The Buy method will throw an exception if the stock is not active or if the amount or price is invalid.
        oldStock.buy(stock.getQuantity(), stock.getPrice());
        log.info("Stock after purchase: {}", oldStock);
        return stockCommandPort.registerPurchase(oldStock);
    }

    @Override
    public Stock save(Stock stock) {
        if (stockQueryPort.existsByProductId(stock.getProductId())) {
            formatterResultOutputPort.returnEntityAlreadyExistsErrorResponse(400, "El Producto con el id " + stock.getProductId() + " ya existe.");
        }
        return stockCommandPort.save(stock);
    }

    @Override
    public Stock update(Long productId, String name) {
        if (!stockQueryPort.existsByProductId(productId)) {
            formatterResultOutputPort.returnEntityDoesNotExistErrorResponse(404, "El Producto con el id " + productId + " no existe.");
        }
        return stockCommandPort.update(productId, name);
    }
}

