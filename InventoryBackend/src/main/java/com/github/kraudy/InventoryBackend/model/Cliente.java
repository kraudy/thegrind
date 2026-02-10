package com.github.kraudy.InventoryBackend.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String apellido;
  private String telefono;
  private String correo;
  private String direccion;
  private Timestamp fechaCreacion;
  private Timestamp fechaModificacion;
}
