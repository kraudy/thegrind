package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdenDTO(
  Long id,
  Long idCliente,
  String clienteNombre,
  String creadaPor,
  BigDecimal totalMonto,
  int totalProductos,
  LocalDateTime fechaCreacion,
  LocalDateTime fechaEntrega,
  LocalDateTime fechaPreparada,
  LocalDateTime fechaDespachada,
  LocalDateTime fechaModificacion,
  String estado
) {}