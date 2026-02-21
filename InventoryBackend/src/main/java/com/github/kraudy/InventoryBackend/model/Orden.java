package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Orden", uniqueConstraints = {}) // si necesitas constraints adicionales aquí
public class Orden {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "id_cliente", nullable = false)
  private Cliente cliente;

  @Transient
  private Long idCliente;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String creadaPor;

  @Column(nullable = false, columnDefinition = "NUMERIC(12,4)")
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
  private LocalDateTime fechaVencimiento;

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
  private String estado = "Recibido"; // Valor por default al ser creada la orden

  // Relación OneToMany con OrdenDetalle
  @JsonIgnore // For now we don't need to return detalle, TODO: this can also be adde to the repository and used it by the controller
  @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrdenDetalle> detalles = new ArrayList<>();

}
