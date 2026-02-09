package com.github.kraudy.InventoryBackend.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrdenDetalle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id_Orden_Detalle;

  // Foreign key to Orden
  private Long Id_Orden;

  // Foreign key to Product
  private Long Id_Producto;

  private int Cantidad;
  private Float Precio_Unitario;
  private Float Subtotal;

  private Timestamp Fecha_Creacion;
  private Timestamp Fecha_Modificacion;
}
