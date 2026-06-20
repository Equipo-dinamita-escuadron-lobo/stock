package com.stock.copy.application.input;

import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;

/**
 * Puerto de entrada: ejecuta una fase de copia de stock.
 * REQ-STOCK-01, ADR-38.
 */
public interface IExecuteStockCopyPhasePort {

    CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request);
}
