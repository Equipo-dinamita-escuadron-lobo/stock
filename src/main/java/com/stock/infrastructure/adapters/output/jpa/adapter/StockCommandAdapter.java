package com.stock.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import com.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import com.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockCommandAdapter implements IStockCommandRepositoryPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;

    @Override
    public Stock save(Stock stock) {
        StockEntity stockEntity = stockEntityMapper.toEntity(stock);
        stockEntity.setId(stock.getProductId());
        StockEntity savedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(savedEntity);
    }

    @Override
    public void inactivate(Long id) {
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        stock.setStatus(false); 
        stockRepository.save(stock);
    }

    @Override
    public void activate(Long id) {
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        stock.setStatus(true);
        stockRepository.save(stock);
    }

    @Override
    public void increaseStock(Long id, int amount) {
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        stock.setQuantity(stock.getQuantity() + amount);
        stockRepository.save(stock);
    }

    @Override
    public void decreaseStock(Long id, int amount) {
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        stock.setQuantity(stock.getQuantity() - amount);
        stockRepository.save(stock);
    }

    @Override
    public void changePrice(Long id, double newPrice) {
        StockEntity stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));
        stock.setPrice(newPrice);
        stockRepository.save(stock);
    }
    
}
