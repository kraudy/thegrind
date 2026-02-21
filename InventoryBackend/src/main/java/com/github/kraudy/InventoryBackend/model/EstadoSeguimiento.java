package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // good practice for natural keys
@Table(name = "EstadoSeguimiento")
public class EstadoSeguimiento {
  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String estado; 

  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  private String descripcion;

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
}
