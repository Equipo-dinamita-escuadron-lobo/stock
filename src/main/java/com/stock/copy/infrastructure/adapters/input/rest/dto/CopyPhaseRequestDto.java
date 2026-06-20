package com.stock.copy.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para ejecutar una fase de copia de stock.
 * Contiene equivalenciasPrev con el remap productId→PRODUCTS (ADR-43).
 * Contrato uniforme REQ-STOCK-02.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseRequestDto {

    @NotNull
    private UUID idProceso;

    @Positive
    private int fase;

    @NotBlank
    private String entOrigen;

    private String entDestino;

    @NotNull
    private Instant snapshotCorte;

    /**
     * Equivalencias previas para remapear productId → nuevo productId en destino.
     * Generadas por PRODUCTS en la fase anterior.
     * REQ-STOCK-02, ADR-43.
     */
    private List<CopyEquivalenciaDto> equivalenciasPrev;

    /**
     * Datos serializados por un BACKUP previo.
     * Presente únicamente en modo RESTORE — null en DUPLICATE y BACKUP.
     */
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Object datosImportados;
}
