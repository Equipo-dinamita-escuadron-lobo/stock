package com.stock.copy.infrastructure.adapters.output.persistence.jpa;

import com.stock.copy.application.output.IStockTargetRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import com.stock.infrastructure.adapters.output.jpa.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA para IStockTargetRepositoryPort.
 * REQ-STOCK-01.
 */
@Component
@RequiredArgsConstructor
public class StockTargetRepositoryAdapter implements IStockTargetRepositoryPort {

    private final StockRepository jpaRepository;

    @Override
    @Transactional
    public StockEntity guardar(StockEntity entity) {
        return jpaRepository.save(entity);
    }
}
