package com.github.kraudy.InventoryBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrdenCostoDTO(
  Long idOrden,
  Long idOrdenDetalle,
  String tipoCosto,
  String trabajador,
  String rol,
  Long idCliente,
  String clienteNombre,
  Long idProducto,
  String productoNombre,
  int cantidadOrden,
  int cantidadAsignada,
  int cantidadTrabajada,
  BigDecimal costo,
  BigDecimal subTotal,
  boolean pagado,
  String usuarioPaga,
  LocalDate fechaPago,
  String comentario,
  LocalDate fechaTrabajo,
  LocalDateTime fechaCreacion,
  String usuarioCreacion,
  LocalDateTime fechaModificacion
) {}
