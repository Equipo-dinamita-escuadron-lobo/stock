package com.stock.copy.application.output;

import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;

/**
 * Puerto de salida: escritura de stock en el tenant destino durante copia.
 * REQ-STOCK-01.
 */
public interface IStockTargetRepositoryPort {

    StockEntity guardar(StockEntity entity);
}
