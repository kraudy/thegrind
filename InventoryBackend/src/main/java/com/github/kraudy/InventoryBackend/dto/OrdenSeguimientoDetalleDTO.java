package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;

public record OrdenSeguimientoDetalleDTO(
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    String nombreProducto,
    int cantidad,
    String tipoProducto,
    String subTipoProducto,
    String estadoActual,
    boolean permiteMover
  ) {}
