package com.github.kraudy.InventoryBackend.model;

import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Orden {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Add foreign key relationship
  //@ManyToOne
  //@JoinColumn(name = "Id_Cliente", referencedColumnName = "Id_Cliente")
  //private Cliente Id_Cliente;
  private Long idCliente;  // Better name than Id_Cliente

  private String recibidaPor;
  private String preparadaPor;
  private String despachadaPor;

  private Float totalMonto;
  private int totalProductos;

  private Timestamp fechaCreacion;
  private Timestamp fechaEntrega;
  private Timestamp fechaPreparada;
  private Timestamp fechaDespachada;
  private Timestamp fechaModificacion;

  private String estado;
}
