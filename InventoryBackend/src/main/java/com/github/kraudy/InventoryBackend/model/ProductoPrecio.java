package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(ProductoPrecioPK.class) // Required for relational mapping with composite key
@Table(name = "ProductoPrecio")
public class ProductoPrecio {
  @Id
  private Long productoId;

  @Id
  @Column(nullable = false, columnDefinition = "NUMERIC(12,4)")
  private BigDecimal precio;

  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  private String descripcion;

  @Column(columnDefinition = "INTEGER")
  private int cantidadRequerida = 0;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;
 
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  @Column(nullable = false)
  private boolean activo = true;

  // === NEW: Many-to-one relationship back to Producto ===
  //TODO: Move this to producto precion repository and call it in the controller to get all the prices of a product
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productoId", referencedColumnName = "id", insertable = false, updatable = false)
  private Producto producto;
}
