package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenPagoDTO(
    Long id,
    Long idOrden,
    String clienteNombre,
    BigDecimal totalMonto,
    BigDecimal monto,
    LocalDateTime fechaPago,
    String metodoPago,
    String codigoReferencia,
    String banco,
    String estado,
    String recibidoPor,
    String aprobadoPor,
    LocalDateTime fechaAprobacion,
    String notas
) {}