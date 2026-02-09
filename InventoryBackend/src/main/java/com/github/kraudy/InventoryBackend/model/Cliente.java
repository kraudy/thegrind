package com.github.kraudy.InventoryBackend.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id_Cliente;

  private String Nombre;
  private String Apellido;
  private String Telefono;
  private String Correo;
  private String Direccion;
  private Timestamp Fecha_Creacion;
  private Timestamp Fecha_Modificacion;
}
