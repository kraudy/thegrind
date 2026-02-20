package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.LocalDate;

public record OrdenCalendarioDTO(
  Long idOrden,
  LocalDateTime fechaTrabajo,
  LocalDate fecha,
  Integer diaTrabajo,
  Integer horaTrabajo,
  Integer minutoTrabajo,
  String clienteNombre,
  LocalDateTime fechaVencimiento,
  LocalDateTime fechaCreacion,
  String usuarioCreacion,
  LocalDateTime fechaModificacion,
  String usuarioModificacion


) { }