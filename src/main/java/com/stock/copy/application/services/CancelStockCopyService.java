package com.stock.copy.application.services;

import com.stock.copy.application.input.ICancelStockCopyPort;
import com.stock.copy.application.output.ICopyJobLogRepositoryPort;
import com.stock.copy.domain.enums.CopyEstado;
import com.stock.copy.domain.exceptions.DuplicateCopyJobException;
import com.stock.copy.domain.models.CopyJobLog;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyCancelResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio: cancela un proceso de copia de stock.
 * REQ-STOCK-03.
 */
@Service
@RequiredArgsConstructor
public class CancelStockCopyService implements ICancelStockCopyPort {

    private final ICopyJobLogRepositoryPort logRepo;

    @Override
    public CopyCancelResponseDto cancelar(String idProceso) {
        CopyJobLog log = logRepo.buscarPorIdProceso(idProceso)
                .orElseThrow(() -> new DuplicateCopyJobException(idProceso, 0));

        CopyJobLog cancelado = CopyJobLog.builder()
                .idProceso(log.getIdProceso())
                .fase(log.getFase())
                .modulo(log.getModulo())
                .estado(CopyEstado.CANCELADO)
                .fechaInicio(log.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(log.getEquivalenciasGeneradas())
                .build();
        logRepo.guardar(cancelado);

        return CopyCancelResponseDto.builder()
                .estado(CopyEstado.CANCELADO.name())
                .mensaje("Proceso de copia stock cancelado exitosamente")
                .build();
    }
}
