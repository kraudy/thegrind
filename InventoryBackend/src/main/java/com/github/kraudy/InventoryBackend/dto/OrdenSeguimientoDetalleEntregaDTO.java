package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;


public record OrdenSeguimientoDetalleEntregaDTO (
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    String nombreProducto,
    int cantidad,
    BigDecimal precioUnitario,
    String tipoProducto,
    String subTipoProducto,
    String estadoActual,

    String trabajadorActual,
    int cantidadAsignadaActual,
    int cantidadTrabajadaActual,

    String estadoPrevio,
    String trabajadorPrevio,
    int cantidadAsignadaPrevio,
    int cantidadTrabajadaPrevio,

    // Trabajo en estado='Entregado' (donde se acumula la cantidad realmente entregada en el mostrador)
    String trabajadorEntrega,
    int cantidadAsignadaEntrega,
    int cantidadEntregada,

    boolean permiteMover
  ) {}