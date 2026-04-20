package com.github.kraudy.InventoryBackend.dto;

import java.time.LocalDateTime;

public record OrdenSeguimientoEstadosGeneralDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String estadoOrden,
    String creadaPor,
    LocalDateTime fechaVencimiento,
    String tiempoRestante,

    boolean tieneRepartidas,
    boolean tieneNormales,
    boolean tieneReparacion,
    boolean tieneImpresion,
    boolean tieneBodega,
    boolean tieneArmado,
    boolean tieneCalado,
    boolean tieneSublimacion,
    boolean tieneEnmarcado,
    boolean tienePegado,
    boolean tieneListo,
    boolean tieneEntregado,

    Long countRepartidas,
    Long countNormales,
    Long countReparacion,
    Long countImpresion,
    Long countBodega,
    Long countArmado,
    Long countCalado,
    Long countSublimacion,
    Long countEnmarcado,
    Long countPegado,
    Long countListo,
    Long countEntregado
) {}
