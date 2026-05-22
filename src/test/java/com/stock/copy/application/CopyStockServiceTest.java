package com.stock.copy.application;

import com.stock.copy.application.input.IExecuteStockCopyPhasePort;
import com.stock.copy.application.output.ICopyJobLogRepositoryPort;
import com.stock.copy.application.output.IStockSourceRepositoryPort;
import com.stock.copy.application.output.IStockTargetRepositoryPort;
import com.stock.copy.application.services.CopyStockService;
import com.stock.copy.domain.enums.CopyEstado;
import com.stock.copy.domain.models.CopyJobLog;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyEquivalenciaDto;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseRequestDto;
import com.stock.copy.infrastructure.adapters.input.rest.dto.CopyPhaseResponseDto;
import com.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TDD RED → GREEN: tests unitarios para CopyStockService.
 * Stock tiene FK productId → PRODUCTS que debe remapearse (ADR-43).
 * REQ-STOCK-01, REQ-STOCK-02.
 */
@ExtendWith(MockitoExtension.class)
class CopyStockServiceTest {

    @Mock
    private ICopyJobLogRepositoryPort logRepo;
    @Mock
    private IStockSourceRepositoryPort sourceRepo;
    @Mock
    private IStockTargetRepositoryPort targetRepo;

    private IExecuteStockCopyPhasePort service;

    @BeforeEach
    void setUp() {
        service = new CopyStockService(logRepo, sourceRepo, targetRepo);
    }

    @Test
    @DisplayName("copia exitosa con remap productId: stock copiado con productId del destino")
    void ejecutar_remapProductId_completado() {
        UUID idProceso = UUID.randomUUID();
        String origen = "empresa-A";
        String destino = "empresa-B";
        Instant snapshot = Instant.now();

        // Equivalencia: producto 10 (origen) → producto 99 (destino)
        CopyEquivalenciaDto equivProduct = CopyEquivalenciaDto.builder()
                .modulo("products")
                .tabla("product")
                .idViejo("10")
                .idNuevo("99")
                .build();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen(origen)
                .entDestino(destino)
                .snapshotCorte(snapshot)
                .equivalenciasPrev(List.of(equivProduct))
                .build();

        StockEntity s1 = new StockEntity();
        s1.setId(1L);
        s1.setProductId(10L);
        s1.setQuantity(100);
        s1.setPrice(BigDecimal.valueOf(50.0));
        s1.setState(true);
        s1.setTenantId(origen);

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(Optional.empty());
        when(sourceRepo.findByEntOrigenBeforeSnapshot(origen, snapshot)).thenReturn(List.of(s1));
        when(targetRepo.guardar(any(StockEntity.class))).thenAnswer(inv -> {
            StockEntity e = inv.getArgument(0);
            e.setId(50L);
            return e;
        });
        when(logRepo.guardar(any(CopyJobLog.class))).thenAnswer(inv -> inv.getArgument(0));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo(CopyEstado.COMPLETADO.name());
        assertThat(response.getRegistrosProcesados()).isEqualTo(1);

        // Verificar que el productId fue remapeado a 99
        verify(targetRepo).guardar(argThat(e -> e.getProductId().equals(99L)));
    }

    @Test
    @DisplayName("productId sin equivalencia: genera advertencia y pone null")
    void ejecutar_productIdSinEquivalencia_advertencia() {
        UUID idProceso = UUID.randomUUID();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList()) // sin equivalencias
                .build();

        StockEntity s = new StockEntity();
        s.setId(1L);
        s.setProductId(10L); // no tiene equivalencia
        s.setQuantity(5);
        s.setPrice(BigDecimal.valueOf(10.0));
        s.setState(true);
        s.setTenantId("A");

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(Optional.empty());
        when(sourceRepo.findByEntOrigenBeforeSnapshot(eq("A"), any())).thenReturn(List.of(s));
        when(targetRepo.guardar(any())).thenAnswer(inv -> {
            StockEntity e = inv.getArgument(0);
            e.setId(20L);
            return e;
        });
        when(logRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo(CopyEstado.COMPLETADO_CON_ADVERTENCIAS.name());
        assertThat(response.getAdvertencias()).isNotEmpty();
        // productId guardado debe ser null (sin equivalencia)
        verify(targetRepo).guardar(argThat(e -> e.getProductId() == null));
    }

    @Test
    @DisplayName("idempotencia: retorna resultado previo si la fase ya fue ejecutada")
    void ejecutar_idempotencia_retornaResultadoPrevio() {
        UUID idProceso = UUID.randomUUID();

        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(idProceso)
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .build();

        CopyJobLog previo = CopyJobLog.builder()
                .idProceso(idProceso)
                .fase(3)
                .estado(CopyEstado.COMPLETADO)
                .equivalenciasGeneradas(3)
                .build();

        when(logRepo.buscarPorIdProcesoYFase(idProceso.toString(), 3)).thenReturn(Optional.of(previo));

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo(CopyEstado.COMPLETADO.name());
        assertThat(response.getMensaje()).contains("idempotencia");
        verify(sourceRepo, never()).findByEntOrigenBeforeSnapshot(any(), any());
    }

    @Test
    @DisplayName("error: origen igual a destino retorna ERROR_NO_REINTENTABLE")
    void ejecutar_origenIgualDestino_errorNoReintentable() {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("empresa-A")
                .entDestino("empresa-A")
                .snapshotCorte(Instant.now())
                .build();

        CopyPhaseResponseDto response = service.ejecutar(request);

        assertThat(response.getEstado()).isEqualTo("ERROR_NO_REINTENTABLE");
        verify(logRepo, never()).guardar(any());
    }
}
