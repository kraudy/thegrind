package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;

public record OrdenSeguimientoFacturacionDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String creadaPor,

    BigDecimal totalMontoOrden,
    BigDecimal totalMontoFactura,

    int totalProductosOrden,
    Long totalProductosFactura,

    LocalDateTime fechaCreacion,
    LocalDateTime fechaVencimiento,
    LocalDateTime fechaLista,        // fecha_preparada
    LocalDateTime fechaEntregada,    // fecha_despachada
    LocalDateTime fechaModificacion,
    String estado,
    String tiempoRestante,
    String duracionTrabajo           // renamed to match query alias
) {}