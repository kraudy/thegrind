package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenDetalleDTO(
  Long idOrden,
  Long idProducto,
  String nombreProducto,
  Long idOrdenDetalle,
  int cantidad,
  BigDecimal precioUnitario,
  BigDecimal subtotal,
  LocalDateTime fechaCreacion,
  LocalDateTime fechaModificacion
) { }
