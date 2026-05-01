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
@IdClass(OrdenCostoPK.class)
@Table(name = "OrdenCosto")
public class OrdenCosto {
  @Id
  @Column(name = "id_orden", nullable = false)
  private Long idOrden;

  @Id
  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  @Id
  @EqualsAndHashCode.Include
  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipoCosto;

  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private String trabajador;

  @Column(nullable = false, columnDefinition = "VARCHAR(30)")
  private String rol;

  @Column(name = "id_producto", nullable = false)
  private Long idProducto;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadOrden;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadAsignada;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidadTrabajada;

  @Column(nullable = false, columnDefinition = "NUMERIC(12,4)")
  private BigDecimal costo;

  @Column(nullable = false, columnDefinition = "BOOLEAN")
  private boolean pagado;

  @Column(nullable = true, columnDefinition = "VARCHAR(50)")
  private String usuarioPaga;

  @Column(nullable = true, columnDefinition = "DATE")
  private LocalDate fechaPago;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String comentario;

  @Column(nullable = false, columnDefinition = "DATE")
  private LocalDate fechaTrabajo;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;

  @Column(nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuarioCreacion;
 
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
  @JoinColumn(name = "id_producto", referencedColumnName = "id", insertable = false, updatable = false)
  private Producto producto;

}
