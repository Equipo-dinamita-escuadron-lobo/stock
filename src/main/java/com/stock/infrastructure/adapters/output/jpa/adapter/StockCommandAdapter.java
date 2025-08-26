package com.stock.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import com.stock.application.ports.input.IStockStatusPort;
import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockCommandRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import com.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import com.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockCommandAdapter implements IStockCommandRepositoryPort, IStockStatusPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;

    @Override
    public Stock save(Stock stock) {
        StockEntity stockEntity = stockEntityMapper.toEntity(stock);
        StockEntity savedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Stock update(Long productId, String name) {
        StockEntity stockEntity = stockRepository.findByProductId(productId);
        stockEntity.setName(name);
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }

    @Override
    public void inactivate(Long productId) {
        StockEntity stock = stockRepository.findByProductId(productId);
        if(stock.isStatus() == true) {
            stock.setStatus(false);
            stockRepository.save(stock);
        } 
    }

    @Override
    public void activate(Long productId) {
        StockEntity stock = stockRepository.findByProductId(productId);
        if(stock.isStatus() == false) {
            stock.setStatus(true);
            stockRepository.save(stock);
        }
    }

    @Override
    public Stock registerSale(Stock stock) {
        StockEntity stockEntity = stockRepository.findByProductId(stock.getProductId());
        stockEntity.setQuantity(stock.getQuantity());
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }


    @Override
    public Stock registerPurchase(Stock stock) {
        StockEntity stockEntity = stockRepository.findByProductId(stock.getProductId());
        stockEntity.setQuantity(stock.getQuantity());
        stockEntity.setPrice(stock.getPrice());
        StockEntity updatedEntity = stockRepository.save(stockEntity);
        return stockEntityMapper.toDomain(updatedEntity);
    }
    
}
