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
    public Stock registerSale(Stock stock) {
        StockEntity stockEntity = stockRepository.findById(stock.getId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + stock.getProductId()));
        
        stockEntity.setQuantity(stock.getQuantity());
        
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }


    @Override
    public Stock registerPurchase(Stock stock) {
        StockEntity stockEntity = stockRepository.findById(stock.getId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + stock.getProductId()));
        
        stockEntity.setQuantity(stock.getQuantity());
        stockEntity.setPrice(stock.getPrice());
        
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
        
    }
    
}
