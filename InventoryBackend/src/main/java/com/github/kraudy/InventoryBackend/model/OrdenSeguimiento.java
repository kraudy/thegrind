package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@IdClass(OrdenSeguimientoPK.class)
@Table(name = "orden_seguimiento")
public class OrdenSeguimiento {

  @Id
  @Column(name = "id_orden")
  private Long idOrden;

  @Id
  @Column(name = "id_orden_detalle")
  private Long idOrdenDetalle;

  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipo;

  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String subTipo;

  @Column(nullable = false, columnDefinition = "INTEGER CHECK (secuencia > 0)")
  private int secuencia;

  @Column(name = "estado", nullable = false, length = 100)
  private String estado;

  // Track who created the tracking record, required and not updatable
  @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(100)")
  private String seguimientoPor;

  // Automatically set the creation timestamp when a new tracking record is created
  @CreationTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  // Relations

  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", insertable = false, updatable = false),
      @JoinColumn(name = "id_orden_detalle", referencedColumnName = "id_orden_detalle", insertable = false, updatable = false)
  })
  private OrdenDetalle ordenDetalle;

}