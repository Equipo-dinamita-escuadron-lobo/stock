package com.stock.copy.infrastructure.adapters.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO equivalencia: tabla + idViejo → idNuevo.
 * Contrato uniforme REQ-STOCK-02.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyEquivalenciaDto {

    private String modulo;
    private String tabla;
    private String idViejo;
    private String idNuevo;
}
