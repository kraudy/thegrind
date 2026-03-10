package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@IdClass(OrdenTrabajoPK.class)
@Table(name = "OrdenTrabajo")
public class OrdenTrabajo {
  @Id
  @Column(name = "id_orden", nullable = false)
  private Long idOrden;

  @Id
  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String estado;

  @Column(nullable = false, columnDefinition = "INTEGER CHECK (secuencia > 0)")
  private int secuencia;

  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private String trabajador;

  @Column(nullable = false, columnDefinition = "VARCHAR(30)")
  private String rol;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private Long idProducto; //TODO: Add FK to producto

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadAsignada;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadTrabajada;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadNoTrabajada;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String comentario;

  @Column(nullable = false, columnDefinition = "DATE")
  private LocalDate fechaTrabajo;

  //TODO: Add FK to ordenSeguimientoHistorico
  private Long idSeguimiento;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;
 
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  // Relations

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", insertable = false, updatable = false),
      @JoinColumn(name = "id_orden_detalle", referencedColumnName = "id_orden_detalle", insertable = false, updatable = false)
  })
  private OrdenDetalle ordenDetalle;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "estado", nullable = false, insertable = false, updatable = false)
  private EstadoSeguimiento estadoSeguimiento;


}
