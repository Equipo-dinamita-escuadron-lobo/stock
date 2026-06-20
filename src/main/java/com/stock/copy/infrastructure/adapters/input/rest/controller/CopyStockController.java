package com.stock.copy.infrastructure.adapters.input.rest.controller;

import com.stock.copy.application.input.*;
import com.stock.copy.domain.exceptions.DuplicateCopyJobException;
import com.stock.copy.infrastructure.adapters.input.rest.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST del bounded context copy en stock.
 * Expone los 4 endpoints del contrato uniforme bajo /api/stock/copy.
 * REQ-STOCK-03, ADR-38.
 */
@RestController
@RequestMapping("/api/stock/copy")
@RequiredArgsConstructor
@Slf4j
public class CopyStockController {

    private final IExecuteStockCopyPhasePort executePort;
    private final IGetStockCopyStatusPort statusPort;
    private final ICancelStockCopyPort cancelPort;
    private final ICleanupStockCopyPort cleanupPort;

    /**
     * POST /api/stock/copy/phase
     * Recibe equivalenciasPrev con remap productId→PRODUCTS (ADR-43).
     */
    @PostMapping("/phase")
    public ResponseEntity<CopyPhaseResponseDto> executePhase(
            @Valid @RequestBody CopyPhaseRequestDto request) {
        log.info("Ejecutando fase {} para proceso {} en stock", request.getFase(), request.getIdProceso());
        CopyPhaseResponseDto response = executePort.ejecutar(request);
        HttpStatus status = resolverHttpStatus(response.getEstado());
        return ResponseEntity.status(status).body(response);
    }

    /** GET /api/stock/copy/{idProceso}/status */
    @GetMapping("/{idProceso}/status")
    public ResponseEntity<CopyStatusResponseDto> getStatus(@PathVariable String idProceso) {
        return ResponseEntity.ok(statusPort.obtenerEstado(idProceso));
    }

    /** POST /api/stock/copy/{idProceso}/cancel */
    @PostMapping("/{idProceso}/cancel")
    public ResponseEntity<CopyCancelResponseDto> cancel(@PathVariable String idProceso) {
        return ResponseEntity.ok(cancelPort.cancelar(idProceso));
    }

    /** DELETE /api/stock/copy/{idProceso}/cleanup */
    @DeleteMapping("/{idProceso}/cleanup")
    public ResponseEntity<Void> cleanup(@PathVariable String idProceso) {
        cleanupPort.limpiar(idProceso);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicateCopyJobException.class)
    public ResponseEntity<String> handleNotFound(DuplicateCopyJobException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    private HttpStatus resolverHttpStatus(String estado) {
        if (estado == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (estado) {
            case "COMPLETADO", "COMPLETADO_CON_ADVERTENCIAS" -> HttpStatus.OK;
            case "ERROR_NO_REINTENTABLE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "ERROR_REINTENTABLE" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
    }
}
