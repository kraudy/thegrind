package com.github.kraudy.InventoryBackend.dto.OrdenCalendario;

import java.time.LocalDate;
import java.util.List;

public record EstadisticasDistribucionHoyDTO(
  long ordenesRecibidas,
  
  List<TrabajadorCargaDTO> reparadores,
  List<TrabajadorCargaDTO> normales,
  List<TrabajadorCargaDTO> repartidas,

  long impresionNormal,
  long impresionReparacion,

  long bodega,
  long armado,
  long calado,

  long pegado,
  long enmarcado,

  long alistado
) {}