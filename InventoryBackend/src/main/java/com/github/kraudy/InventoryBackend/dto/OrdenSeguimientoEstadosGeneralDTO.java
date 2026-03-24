package com.github.kraudy.InventoryBackend.dto;

import java.time.LocalDateTime;

public record OrdenSeguimientoEstadosGeneralDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String creadaPor,
    LocalDateTime fechaVencimiento,
    String tiempoRestante,
    boolean tieneRepartidas,
    boolean tieneNormales,
    boolean tieneReparacion,
    boolean tieneImpresion,
    boolean tieneEnmarcado,
    boolean tienePegado,
    boolean tieneListo,
    boolean tieneEntregado
) {}
