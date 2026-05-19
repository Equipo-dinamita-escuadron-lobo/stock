package com.stock.copy.application.input;

import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyStatusResponseDto;

/**
 * Puerto de entrada: consulta estado de copia de stock.
 * REQ-STOCK-03.
 */
public interface IGetStockCopyStatusPort {

    CopyStatusResponseDto obtenerEstado(String idProceso);
}
