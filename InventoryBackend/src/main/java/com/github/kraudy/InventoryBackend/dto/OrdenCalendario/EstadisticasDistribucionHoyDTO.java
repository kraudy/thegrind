package com.github.kraudy.InventoryBackend.dto.OrdenCalendario;

import java.time.LocalDate;
import java.util.List;

public record EstadisticasDistribucionHoyDTO(
  long ordenesRecibidas,
  List<TrabajadorCargaDTO> reparadores,
  List<TrabajadorCargaDTO> normales,
  long impresionNormal,
  long impresionReparacion
) {}