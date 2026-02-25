package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(OrdenSeguimientoHistoricoPK.class)
@Table(name = "orden_seguimiento_historico")
public class OrdenSeguimientoHistorico {
  @Id
  @Column(name = "id_orden")
  private Long idOrden;

  @Id
  @Column(name = "id_orden_detalle")
  private Long idOrdenDetalle;

  @Id
  @Column(name = "estado", nullable = false, length = 100)
  private String estado;

  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioCreacion;

  @Column(nullable = false)
  private LocalDateTime fechaFinalizacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String usuarioFinalizacion;

  @Column(nullable = false)
  private Long duracion;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaRegistro; // Cuando se creo el registro

}
