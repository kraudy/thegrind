package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "OrdenCalendario") 
public class OrdenCalendario {
  @Id
  private Long idOrden;

  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaTrabajo;

  @Column(nullable = false, columnDefinition = "DATE")
  private LocalDate fecha;

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

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "id", nullable = false)
  private Orden orden;
}
