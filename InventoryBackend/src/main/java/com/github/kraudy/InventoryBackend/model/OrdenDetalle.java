package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(OrdenDetallePK.class)
@Table(name = "OrdenDetalle")
public class OrdenDetalle {
  @Id
  @Column(name = "id_orden", insertable = false, updatable = false)
  private Long idOrden;

  @Id
  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  @Column(name = "id_producto", insertable = false, updatable = false)
  private Long idProducto;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidad;

  @Column(nullable = false, columnDefinition = "DECIMAL(12,4)")
  private BigDecimal precioUnitario;

  @Column(nullable = false, columnDefinition = "DECIMAL(12,4)")
  private BigDecimal subtotal;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime fechaModificacion;

  // ================== Relaciones ==================
  //@JsonIgnore
  @JsonProperty(access = Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("idOrden")
  @JoinColumn(name = "id_orden", nullable = false)
  private Orden orden;

  //@JsonIgnore
  @JsonProperty(access = Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_producto", referencedColumnName = "id", nullable = false)
  private Producto producto;

  //TODO: Creo que este no lo necesito porque al final el seguimiento lo borro y queda en el historico
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumns({
      @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", insertable = false, updatable = false),
      @JoinColumn(name = "id_orden_detalle", referencedColumnName = "id_orden_detalle", insertable = false, updatable = false)
  })
  private OrdenSeguimiento seguimientos;
}
