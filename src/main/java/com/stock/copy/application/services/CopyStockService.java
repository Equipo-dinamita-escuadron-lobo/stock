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

import java.math.BigDecimal;
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
        // Despacho por modo: RESTORE → BACKUP → DUPLICATE
        if (request.getDatosImportados() != null) {
            return ejecutarImportacion(request);
        }
        if (request.getEntDestino() == null || request.getEntDestino().isBlank()) {
            return ejecutarExportacion(request);
        }
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
                nuevo.setState(original.isState());
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
    // BACKUP: exportar datos del tenant origen
    // ----------------------------------------------------------------

    private CopyPhaseResponseDto ejecutarExportacion(CopyPhaseRequestDto request) {
        log.info("Modo BACKUP stock — exportando datos de entOrigen={}", request.getEntOrigen());

        List<StockEntity> origenList = sourceRepo.findByEntOrigenBeforeSnapshot(
                request.getEntOrigen(), request.getSnapshotCorte());

        List<Map<String, Object>> registros = new ArrayList<>();
        for (StockEntity e : origenList) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", e.getId());
            row.put("productId", e.getProductId());
            row.put("name", e.getName());
            row.put("quantity", e.getQuantity());
            row.put("price", e.getPrice());
            row.put("state", e.isState());
            registros.add(row);
        }

        Map<String, Object> datosExportados = new HashMap<>();
        datosExportados.put("stock", registros);

        return CopyPhaseResponseDto.builder()
                .estado("COMPLETADO")
                .registrosProcesados(registros.size())
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Modo BACKUP — " + registros.size() + " registros stock exportados")
                .advertencias(Collections.emptyList())
                .datosExportados(datosExportados)
                .build();
    }

    // ----------------------------------------------------------------
    // RESTORE: importar datos serializados en el tenant destino
    // ----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private CopyPhaseResponseDto ejecutarImportacion(CopyPhaseRequestDto request) {
        log.info("Modo RESTORE stock — importando en entDestino={}", request.getEntDestino());

        String idProceso = request.getIdProceso().toString();

        // Idempotencia
        Optional<CopyJobLog> previo = logRepo.buscarPorIdProcesoYFase(idProceso, request.getFase());
        if (previo.isPresent()) {
            log.info("Fase {} del proceso {} ya fue ejecutada (RESTORE) — idempotencia",
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

        List<String> advertencias = new ArrayList<>();
        List<CopyEquivalenciaDto> equivalencias = new ArrayList<>();

        String tenantOriginal = TenantContext.getTenantId();
        TenantContext.setTenantId(request.getEntDestino());

        int totalRegistros = 0;

        try {
            Map<String, Object> datos = (Map<String, Object>) request.getDatosImportados();
            List<Map<String, Object>> registros = (List<Map<String, Object>>) datos.get("stock");

            if (registros != null) {
                for (Map<String, Object> row : registros) {
                    Long idOriginal = toLong(row.get("id"));
                    Long productIdOriginal = toLong(row.get("productId"));

                    // Remapear productId usando equivalenciasPrev (tabla "product")
                    Long productIdNuevo = remapearFkPrev(
                            productIdOriginal, "product",
                            request.getEquivalenciasPrev(), advertencias, idOriginal);

                    StockEntity nuevo = new StockEntity();
                    nuevo.setId(null);
                    nuevo.setProductId(productIdNuevo);
                    nuevo.setQuantity(toInt(row.get("quantity")));
                    nuevo.setPrice(toBigDecimal(row.get("price")));
                    nuevo.setState(toBool(row.get("state")));
                    nuevo.setEnterpriseId(request.getEntDestino());
                    nuevo.setName(toStr(row.get("name")));

                    StockEntity guardado = targetRepo.guardar(nuevo);

                    equivalencias.add(CopyEquivalenciaDto.builder()
                            .modulo(MODULO)
                            .tabla(MODULO)
                            .idViejo(String.valueOf(idOriginal))
                            .idNuevo(String.valueOf(guardado.getId()))
                            .build());
                    totalRegistros++;
                }
            }

        } catch (Exception e) {
            log.error("Error inesperado durante RESTORE stock proceso {}: {}", idProceso, e.getMessage(), e);
            registrarFallo(request, e.getMessage(), logInicio.getFechaInicio());
            return CopyPhaseResponseDto.builder()
                    .estado("ERROR_REINTENTABLE")
                    .mensaje("Error interno RESTORE: " + e.getMessage())
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
                .mensaje("RESTORE stock completado exitosamente")
                .advertencias(advertencias)
                .build();
    }

    // ----------------------------------------------------------------
    // Helper FK cross-service desde equivalenciasPrev
    // ----------------------------------------------------------------

    private Long remapearFkPrev(Long idViejo, String tabla,
                                 List<CopyEquivalenciaDto> equivPrev,
                                 List<String> advertencias, Long entidadId) {
        if (idViejo == null) return null;
        if (equivPrev == null || equivPrev.isEmpty()) {
            advertencias.add("Entidad " + entidadId + " FK tabla='" + tabla
                    + "' idViejo=" + idViejo + " sin equivalencia previa; insertado null.");
            return null;
        }
        return equivPrev.stream()
                .filter(e -> tabla.equals(e.getTabla()) && String.valueOf(idViejo).equals(e.getIdViejo()))
                .map(e -> e.getIdNuevo() != null ? Long.parseLong(e.getIdNuevo()) : null)
                .findFirst()
                .orElseGet(() -> {
                    advertencias.add("Entidad " + entidadId + " FK tabla='" + tabla
                            + "' idViejo=" + idViejo + " sin equivalencia previa; insertado null.");
                    return null;
                });
    }

    // ----------------------------------------------------------------
    // Helpers de conversión de tipos (JSON deserializado como Object)
    // ----------------------------------------------------------------

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Number n) return n.longValue();
        return null;
    }

    private int toInt(Object v) {
        if (v instanceof Integer i) return i;
        if (v instanceof Number n) return n.intValue();
        return 0;
    }

    private String toStr(Object v) { return v != null ? v.toString() : null; }

    private boolean toBool(Object v) { return v instanceof Boolean b && b; }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Double d) return BigDecimal.valueOf(d);
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
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
