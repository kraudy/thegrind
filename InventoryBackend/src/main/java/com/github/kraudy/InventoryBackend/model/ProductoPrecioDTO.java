package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoPrecioDTO(
    Long productoId,
    BigDecimal precio,
    String descripcion,
    int cantidadRequerida,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion,
    boolean activo,
    String productoNombre,
    String productoTipo  // or whatever type
) {}