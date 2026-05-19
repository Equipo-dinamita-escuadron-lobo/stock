package com.stock.copy.application.services;

import com.stock.copy.application.input.IExecuteStockCopyPhasePort;
import com.stock.copy.application.output.ICopyJobLogRepositoryPort;
import com.stock.copy.application.output.IStockSourceRepositoryPort;
import com.stock.copy.application.output.IStockTargetRepositoryPort;
import com.stock.copy.domain.enums.CopyEstado;
import com.stock.copy.domain.models.CopyJobLog;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import com.stock.infrastructure.adapters.output.multitenancy.utils.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que orquesta la copia del módulo stock.
 *
 * Stock tiene FK productId → PRODUCTS. Se remapea usando equivalenciasPrev
 * tabla "product" (Opción A: request rico, ADR-35/36).
 *
 * Si productId no tiene equivalencia: se inserta null y se genera advertencia.
 *
 * AMQP: CopyActiveFlag suprime mensajes entrantes durante la copia.
 *
 * REQ-STOCK-01, REQ-STOCK-02, ADR-38, ADR-43.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CopyStockService implements IExecuteStockCopyPhasePort {

    private static final String MODULO = "stock";

    private final ICopyJobLogRepositoryPort logRepo;
    private final IStockSourceRepositoryPort sourceRepo;
    private final IStockTargetRepositoryPort targetRepo;

    @Override
    public CopyPhaseResponseDto ejecutar(CopyPhaseRequestDto request) {
        if (request.getEntOrigen().equals(request.getEntDestino())) {
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_NO_REINTENTABLE")
                    .mensaje("entOrigen y entDestino no pueden ser iguales")
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        }

        String idProceso = request.getIdProceso().toString();

        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("Fase {} del proceso {} ya fue ejecutada — retornando resultado previo (idempotencia)",
                    request.getFase(), idProceso);
            return construirResponseDesdeLog(previo.get());
        }

        CopyJobLog logInicio = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(CopyEstado.EN_PROCESO)
                .fechaInicio(Instant.now())
                .equivalenciasGeneradas(0)
                .build();
        logRepo.guardar(logInicio);

        // Construir índice productId (tabla "product") para remap
        Map<String, Long> productIndex = construirIndiceProducto(request.getEquivalenciasPrev());

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();

        String tenantOriginal = TenantContext.getTenantId();
        TenantContext.setTenantId(request.getEntDestino());

        int totalRegistros = 0;

        try {
            List<StockEntity> origenList = sourceRepo.findByEntOrigenBeforeSnapshot(
                    request.getEntOrigen(), request.getSnapshotCorte());

            for (StockEntity original : origenList) {
                StockEntity nuevo = new StockEntity();
                nuevo.setId(null);
                nuevo.setQuantity(original.getQuantity());
                nuevo.setPrice(original.getPrice());
                nuevo.setStatus(original.isStatus());
                // tenantId lo gestiona Hibernate por @TenantId

                // Remapear productId → nuevo productId
                nuevo.setProductId(
                    remapearProductId(original.getProductId(), original.getId(), advertencias, productIndex));

                StockEntity guardado = targetRepo.guardar(nuevo);
                equivalencias.add(CopyEquivalenciaDto.builder()
                        .modulo(MODULO)
                        .tabla(MODULO)
                        .idViejo(String.valueOf(original.getId()))
                        .idNuevo(String.valueOf(guardado.getId()))
                        .build());
                totalRegistros++;
            }

        } catch (Exception e) {
            log.error("Error inesperado durante copia stock del proceso {}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error interno: " + e.getMessage())
                    .equivalenciasGeneradas(Collections.emptyList())
                    .advertencias(Collections.emptyList())
                    .build();
        } finally {
            if (tenantOriginal != null) {
                TenantContext.setTenantId(tenantOriginal);
            } else {
                TenantContext.clear();
            }
        }

        CopyEstado estadoFinal = advertencias.isEmpty()
                ? CopyEstado.COMPLETADO
                : CopyEstado.COMPLETADO_CON_ADVERTENCIAS;

        CopyJobLog logFin = CopyJobLog.builder()
                .idProceso(request.getIdProceso())
                .fase(request.getFase())
                .modulo(MODULO)
                .estado(estadoFinal)
                .fechaInicio(logInicio.getFechaInicio())
                .fechaFin(Instant.now())
                .equivalenciasGeneradas(equivalencias.size())
                .build();
        logRepo.guardar(logFin);

        return CopyPhaseResponseDto.builder()
                .estado(estadoFinal.name())
                .registrosProcesados(totalRegistros)
                .equivalenciasGeneradas(equivalencias)
                .mensaje("Copia stock completada exitosamente")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Construye índice idViejo → idNuevo para tabla "product" desde equivalenciasPrev.
     */
    private Map<String, Long> construirIndiceProducto(List<CopyEquivalenciaDto> equivalenciasPrev) {
        if (equivalenciasPrev == null) return Collections.emptyMap();
        return equivalenciasPrev.stream()
                .filter(e -> "product".equals(e.getTabla())
                             && e.getIdViejo() != null && e.getIdNuevo() != null)
                .collect(Collectors.toMap(
                        CopyEquivalenciaDto::getIdViejo,
                        e -> Long.parseLong(e.getIdNuevo()),
                        (a, b) -> a));
    }

    /**
     * Remapea productId usando el índice.
     * Si idViejo es null → retorna null.
     * Si no hay equivalencia → registra advertencia y retorna null.
     */
    private Long remapearProductId(Long idViejo, Long stockId,
                                    List<String> advertencias,
                                    Map<String, Long> productIndex) {
        if (idViejo == null) return null;

        Long idNuevo = productIndex.get(String.valueOf(idViejo));
        if (idNuevo == null) {
            String adv = String.format(
                "Stock id=%s: productId=%s sin equivalencia en PRODUCTS; se inserta null.",
                stockId, idViejo);
            log.warn(adv);
            advertencias.add(adv);
        }
        return idNuevo;
    }

    private CopyPhaseResponseDto construirResponseDesdeLog(CopyJobLog log) {
        return CopyPhaseResponseDto.builder()
                .estado(log.getEstado().name())
                .registrosProcesados(log.getEquivalenciasGeneradas() != null ? log.getEquivalenciasGeneradas() : 0)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Resultado de ejecución previa (idempotencia)")
                .advertencias(Collections.emptyList())
                .build();
    }

    private void registrarFallo(CopyPhaseRequestDto request, String mensaje, Instant fechaInicio) {
        try {
            CopyJobLog logFallo = CopyJobLog.builder()
                    .idProceso(request.getIdProceso())
                    .fase(request.getFase())
                    .modulo(MODULO)
                    .estado(CopyEstado.FALLIDO)
                    .fechaInicio(fechaInicio != null ? fechaInicio : Instant.now())
                    .fechaFin(Instant.now())
                    .equivalenciasGeneradas(0)
                    .errorMessage(mensaje)
                    .build();
            logRepo.guardar(logFallo);
        } catch (Exception e) {
            log.error("Error al registrar fallo de copia stock: {}", e.getMessage());
        }
    }
}
