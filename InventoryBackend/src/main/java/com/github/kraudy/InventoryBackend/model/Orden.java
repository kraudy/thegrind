package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
  private Long idCliente;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String recibidaPor; //TODO: Change to creadaPor

  //private String recibidaPor; //TODO: Move this to order detail

  @Column(nullable = true, columnDefinition = "VARCHAR(100)")
  private String preparadaPor; //TODO: Move this to order detail
  @Column(nullable = true, columnDefinition = "VARCHAR(100)")
  private String despachadaPor; //TODO: Move this to order detail

  @Column(nullable = false, columnDefinition = "NUMERIC(12,2)")
  private BigDecimal totalMonto = BigDecimal.ZERO;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int totalProductos = 0;

  // Set creation timestamp automatically when the order is created 
  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;

  // Establecer fecha de entrega propuesta al momento de crear la orden, no puede ser nula.
  // Es necesaria para poder planificar la preparación y despacho de la orden.
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaEntrega;

  // Optional: set when order is prepared
  @Column(nullable = true, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaPreparada;

  // Optional: set when order is dispatched
  @Column(nullable = true, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaDespachada;

  // Update timestamp automatically when the order is updated
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  @Column(nullable = false, columnDefinition = "VARCHAR(12)")
  private String estado = "Pendiente"; // Valor por default al ser creada la orden
}
