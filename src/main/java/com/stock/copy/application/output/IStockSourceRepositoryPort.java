package com.stock.copy.application.output;

import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de salida: lectura de stock del tenant origen para copia.
 * REQ-STOCK-01.
 */
public interface IStockSourceRepositoryPort {

    List<StockEntity> findByEntOrigenBeforeSnapshot(String entOrigen, Instant snapshotCorte);
}
