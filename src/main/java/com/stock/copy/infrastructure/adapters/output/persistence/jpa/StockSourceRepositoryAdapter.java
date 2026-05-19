package com.stock.copy.infrastructure.adapters.output.persistence.jpa;

import com.stock.copy.application.output.IStockSourceRepositoryPort;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Adaptador JPA para IStockSourceRepositoryPort.
 * REQ-STOCK-01.
 */
@Component
@RequiredArgsConstructor
public class StockSourceRepositoryAdapter implements IStockSourceRepositoryPort {

    private final StockCopySourceRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockEntity> findByEntOrigenBeforeSnapshot(String entOrigen, Instant snapshotCorte) {
        return jpaRepository.findByEntOrigenBeforeSnapshot(entOrigen, snapshotCorte);
    }
}
