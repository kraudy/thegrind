package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@IdClass(ProductoCostoPK.class) // Required for relational mapping with composite key
@Table(name = "ProductoCosto")
public class ProductoCosto {
  @Id
  private Long productoId;

  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipoCosto;
  
  @Column(nullable = false, columnDefinition = "NUMERIC(12,4)")
  private BigDecimal costo;

  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  private String descripcion;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadRequerida = 0;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;
 
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  @Column(nullable = false)
  private boolean activo = true;

}
