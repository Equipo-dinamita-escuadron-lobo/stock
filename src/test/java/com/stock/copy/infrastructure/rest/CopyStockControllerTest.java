package com.stock.copy.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stock.copy.application.input.*;
import com.stock.copy.infrastructure.adapters.input.rest.controller.CopyStockController;
import com.stock.copy.infrastructure.adapters.input.rest.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD RED → GREEN: tests del controlador REST CopyStockController.
 * REQ-STOCK-03.
 */
@ExtendWith(MockitoExtension.class)
class CopyStockControllerTest {

    @Mock
    private IExecuteStockCopyPhasePort executePort;
    @Mock
    private IGetStockCopyStatusPort statusPort;
    @Mock
    private ICancelStockCopyPort cancelPort;
    @Mock
    private ICleanupStockCopyPort cleanupPort;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        CopyStockController controller = new CopyStockController(
                executePort, statusPort, cancelPort, cleanupPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/stock/copy/phase retorna 200 COMPLETADO")
    void executePhase_happy_200() throws Exception {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList())
                .build();

        CopyPhaseResponseDto response = CopyPhaseResponseDto.builder()
                .estado("COMPLETADO")
                .registrosProcesados(3)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Copia stock completada exitosamente")
                .advertencias(Collections.emptyList())
                .build();

        when(executePort.ejecutar(any())).thenReturn(response);

        mockMvc.perform(post("/api/stock/copy/phase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.registrosProcesados").value(3));
    }

    @Test
    @DisplayName("POST /api/stock/copy/phase retorna 200 COMPLETADO_CON_ADVERTENCIAS cuando productId sin equivalencia")
    void executePhase_conAdvertencias_200() throws Exception {
        CopyPhaseRequestDto request = CopyPhaseRequestDto.builder()
                .idProceso(UUID.randomUUID())
                .fase(3)
                .entOrigen("A")
                .entDestino("B")
                .snapshotCorte(Instant.now())
                .equivalenciasPrev(Collections.emptyList())
                .build();

        CopyPhaseResponseDto response = CopyPhaseResponseDto.builder()
                .estado("COMPLETADO_CON_ADVERTENCIAS")
                .registrosProcesados(1)
                .equivalenciasGeneradas(Collections.emptyList())
                .mensaje("Copia stock completada exitosamente")
                .advertencias(Collections.singletonList("Stock id=1: productId=10 sin equivalencia; se inserta null."))
                .build();

        when(executePort.ejecutar(any())).thenReturn(response);

        mockMvc.perform(post("/api/stock/copy/phase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO_CON_ADVERTENCIAS"));
    }

    @Test
    @DisplayName("GET /api/stock/copy/{idProceso}/status retorna 200")
    void getStatus_happy_200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(statusPort.obtenerEstado(idProceso)).thenReturn(CopyStatusResponseDto.builder()
                .fase(3).estado("COMPLETADO").registrosProcesados(3).intentos(1).build());

        mockMvc.perform(get("/api/stock/copy/{idProceso}/status", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"));
    }

    @Test
    @DisplayName("POST /api/stock/copy/{idProceso}/cancel retorna 200 CANCELADO")
    void cancel_happy_200() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(cancelPort.cancelar(idProceso)).thenReturn(CopyCancelResponseDto.builder()
                .estado("CANCELADO").mensaje("Proceso de copia stock cancelado exitosamente").build());

        mockMvc.perform(post("/api/stock/copy/{idProceso}/cancel", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }
}
