package com.stock.copy.application.input;

import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyCancelResponseDto;

/**
 * Puerto de entrada: cancela una copia de stock.
 * REQ-STOCK-03.
 */
public interface ICancelStockCopyPort {

    CopyCancelResponseDto cancelar(String idProceso);
}
