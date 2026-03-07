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
@Table(name = "Rol") 
public class Rol {
  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(30)")
  private String rol;

  @Column(columnDefinition = "VARCHAR(255)")
  private String descripcion;

  @CreationTimestamp
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime fechaCreacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioCreacion = "testAdmin";

  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime fechaModificacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioModificacion = "testAdmin";

  @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  private boolean activo = true;
}
