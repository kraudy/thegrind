package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FacturaDetalleDTO(
  Long idFactura,
  Long idDetalle,
  Long idOrdenDetalle,
  Long idProducto,
  String nombreProducto,
  BigDecimal precio,
  int cantidad,
  BigDecimal subtotal,
  String usuarioCreacion,
  LocalDateTime fechaCreacion
) { }

