package com.stock.copy.application.input;

/**
 * Puerto de entrada: limpia registros de copia de stock.
 * REQ-STOCK-03.
 */
public interface ICleanupStockCopyPort {

    void limpiar(String idProceso);
}
