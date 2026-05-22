package com.stock.copy.infrastructure.adapters.output.persistence.jpa;

import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio JPA de solo lectura para StockEntity en contexto de copia.
 * REQ-STOCK-01.
 */
public interface StockCopySourceRepository extends JpaRepository<StockEntity, Long> {

    @Query("SELECT s FROM StockEntity s WHERE s.tenantId = :entOrigen AND (s.createdAt IS NULL OR s.createdAt <= :snapshotCorte)")
    List<StockEntity> findByEntOrigenBeforeSnapshot(
            @Param("entOrigen") String entOrigen,
            @Param("snapshotCorte") Instant snapshotCorte);
}
