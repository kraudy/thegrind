package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Listado combinado de ordenes en estados Listo y Entregado para la pantalla
 * unificada de "Entrega y Facturacion". Incluye los totales de facturacion
 * para que el FE pueda mostrar saldo pendiente sin un segundo round-trip.
 */
public record OrdenEntregaFacturacionDTO(
    Long id,
    Long idCliente,
    String clienteNombre,
    String creadaPor,

    BigDecimal totalMontoOrden,
    BigDecimal totalMontoFactura,
    BigDecimal saldoPendiente,

    int totalProductosOrden,
    Long totalProductosFactura,

    LocalDateTime fechaCreacion,
    LocalDateTime fechaVencimiento,
    LocalDateTime fechaLista,        // fecha_preparada
    LocalDateTime fechaEntregada,    // fecha_despachada
    LocalDateTime fechaModificacion,
    String estado,
    String tiempoRestante,
    String duracionTrabajo,
    String canal,
    String prioridad
) {}
