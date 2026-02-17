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
  LocalDateTime fechaCreacion,
  String usuarioCrea,
  LocalDateTime fechaModificacion,
  String usuarioModifica


) { }