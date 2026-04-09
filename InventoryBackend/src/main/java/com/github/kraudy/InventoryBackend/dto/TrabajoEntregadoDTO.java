package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;

public record TrabajoEntregadoDTO (
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    int cantidadEntregada,
    BigDecimal precio,
    BigDecimal subtotal

  ) {}
