package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDateTime;

public record OrdenSeguimientoDetalleFacturacionDTO(
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    String nombreProducto,
    int cantidadOrden,
    BigDecimal subTotalOrden,
    BigDecimal precioUnitario,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion,
    String tipoProducto,
    String subTipoProducto,

    String trabajadorEntrega,
    int cantidadFactura,
    BigDecimal subtotalFactura
  ) {}