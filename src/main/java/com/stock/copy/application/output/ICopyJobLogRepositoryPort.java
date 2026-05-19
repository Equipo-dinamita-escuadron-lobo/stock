package com.stock.copy.application.output;

import com.stock.copy.domain.models.CopyJobLog;

import java.util.Optional;

/**
 * Puerto de salida: log de idempotencia de copia de stock.
 * REQ-STOCK-01.
 */
public interface ICopyJobLogRepositoryPort {

    CopyJobLog guardar(CopyJobLog log);

    Optional<CopyJobLog> buscarPorIdProcesoYFase(String idProceso, int fase);

    Optional<CopyJobLog> buscarPorIdProceso(String idProceso);

    void eliminarPorIdProceso(String idProceso);
}
