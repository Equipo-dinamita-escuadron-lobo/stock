package com.stock.infrastructure.adapters.output.jpa.adapter;

import org.springframework.stereotype.Service;

import com.stock.domain.model.Stock;
import com.stock.domain.port.IStockQueryRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.mapper.StockEntityMapper;
import com.stock.infrastructure.adapters.output.jpa.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockQueryAdapter implements IStockQueryRepositoryPort {

    private final StockRepository stockRepository;
    private final StockEntityMapper stockEntityMapper;

    @Override
    public Stock findById(Long id) {
        return stockRepository.findById(id)
                .map(stockEntityMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + id));      
    }

    @Override
    public boolean existsById(Long id) {
        return stockRepository.existsById(id);
    } 
}
