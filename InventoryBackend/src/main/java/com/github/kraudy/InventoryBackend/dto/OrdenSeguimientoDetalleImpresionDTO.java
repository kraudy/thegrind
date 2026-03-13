package com.github.kraudy.InventoryBackend.dto;

public record OrdenSeguimientoDetalleImpresionDTO(
    Long idOrden,
    Long idOrdenDetalle,
    Long idProducto,
    String nombreProducto,
    int cantidad,
    String tipoProducto,
    String subTipoProducto,
    String estadoActual,
    String trabajador,
    int cantidadTrabajada,
    int cantidadPendiente,
    boolean permiteMover
  ) {}