package com.stock.copy.application.services;

import com.stock.copy.application.input.ICleanupStockCopyPort;
import com.stock.copy.application.output.ICopyJobLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio: limpia registros de copia de stock.
 * REQ-STOCK-03.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupStockCopyService implements ICleanupStockCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public void limpiar(String idProceso) {
        log.info("Limpiando registros de copia stock para proceso {}", idProceso);
        logRepo.eliminarPorIdProceso(idProceso);
    }
}
