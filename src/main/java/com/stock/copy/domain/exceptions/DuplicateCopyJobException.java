package com.stock.copy.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra el proceso de copia.
 * REQ-STOCK-01.
 */
public class DuplicateCopyJobException extends RuntimeException {

    public DuplicateCopyJobException(String idProceso, int fase) {
        super("No se encontró proceso de copia: idProceso=" + idProceso + ", fase=" + fase);
    }
}
