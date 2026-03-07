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
@Table(name = "orden_seguimiento_historico",
  indexes = {  
    @Index( // Index para ordenes
      name = "idx_id_orden",
      columnList = "id_orden"
    ),
    @Index( // Index para estados
      name = "idx_estado",
      columnList = "estado"
    )
  }
)
public class OrdenSeguimientoHistorico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_orden")
  private Long idOrden;

  @Column(name = "id_orden_detalle")
  private Long idOrdenDetalle;

  @Column(name = "estado", nullable = false, length = 100)
  private String estado;

  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuarioCreacion;

  @Column(nullable = false)
  private LocalDateTime fechaFinalizacion;

  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuarioFinalizacion;

  @Column(nullable = false)
  private Long duracion;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaRegistro; // Cuando se creo el registro

}
