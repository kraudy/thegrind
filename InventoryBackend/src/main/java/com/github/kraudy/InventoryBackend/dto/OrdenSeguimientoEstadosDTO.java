package com.github.kraudy.InventoryBackend.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record OrdenSeguimientoEstadosDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String creadaPor,
    LocalDateTime fechaVencimiento,
    String tiempoRestante,
    boolean tieneNormales,
    boolean tieneReparacion,
    boolean tieneImpresion,
    boolean tieneEnmarcado,
    boolean tienePegado
) {}
