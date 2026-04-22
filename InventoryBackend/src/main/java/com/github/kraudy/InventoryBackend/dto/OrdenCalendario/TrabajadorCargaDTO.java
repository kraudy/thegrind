package com.github.kraudy.InventoryBackend.dto.OrdenCalendario;

import java.time.LocalDate;
import java.util.List;

public record TrabajadorCargaDTO(
  String trabajador,
  long cantidadDetalles
) {}