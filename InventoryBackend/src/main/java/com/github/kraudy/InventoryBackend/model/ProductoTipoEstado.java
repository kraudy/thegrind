package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Check;   

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@IdClass(ProductoTipoEstadoPK.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // good practice for natural keys
@Table(name = "ProductoTipoEstado")
public class ProductoTipoEstado {
  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipo;  
  
  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String estado;  

  @Column(nullable = false, columnDefinition = "INTEGER CHECK (secuencia > 0)")
  private int secuencia;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioCreacion;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime fechaModificacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioModificacion;

  // Relations

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "tipo", nullable = false)
  private ProductoTipo productoTipo;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "estado", nullable = false)
  private EstadoSeguimiento estadoSeguimiento;

}
