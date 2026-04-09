package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FacturaDTO(
  Long id,
  Long idCliente,
  String clienteNombre,
  Long idOrden,
  String usuarioCreacion,
  BigDecimal total,
  LocalDateTime fechaCreacion,
  String estado
) {}
