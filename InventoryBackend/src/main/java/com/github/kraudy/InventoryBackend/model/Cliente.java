package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Cliente", uniqueConstraints = {}) 
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String nombre;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String apellido;

  @Column(nullable = false, columnDefinition = "VARCHAR(15)")
  private String telefono;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String correo;

  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  private String direccion;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;
 
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;
}
