package com.stock.copy.infrastructure.adapters.output.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para el log de idempotencia de copia de stock.
 * REQ-STOCK-01.
 */
public interface CopyJobLogJpaRepository extends JpaRepository<CopyJobLogEntity, Long> {

    Optional<CopyJobLogEntity> findByIdProcesoAndFase(String idProceso, Integer fase);

    Optional<CopyJobLogEntity> findTopByIdProcesoOrderByFechaInicioDesc(String idProceso);

    void deleteByIdProceso(String idProceso);
}
