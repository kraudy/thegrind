package com.github.kraudy.InventoryBackend.dto;


public record OrdenSeguimientoDetalleEntregaDTO (
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    String nombreProducto,
    int cantidad,
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

    boolean permiteMover
  ) {}