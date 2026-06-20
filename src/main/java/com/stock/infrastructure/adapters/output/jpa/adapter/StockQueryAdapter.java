package com.stock.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Component;

import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockQueryRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import com.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockQueryAdapter implements IStockQueryRepositoryPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;

    @Override
    public Stock findByProductId(Long id) {
        return stockRepository.findByProductId(id) != null ? stockEntityMapper.toDomain(stockRepository.findByProductId(id)) : null;
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return stockRepository.existsByProductId(productId);
    } 
}
