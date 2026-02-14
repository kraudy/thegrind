package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
  @Column(name = "id_producto", insertable = false, updatable = false)
  private Long idProducto;

  @Id
  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  // ================== Relaciones con MapsId ==================
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("idOrden")
  @JoinColumn(name = "id_orden", nullable = false)
  private Orden orden;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("idProducto")
  @JoinColumn(name = "id_producto", nullable = false)
  private Producto producto;
  // ================== Relaciones con MapsId ==================

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

  @OneToMany(mappedBy = "ordenDetalle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrdenSeguimiento> seguimientos = new ArrayList<>();
}
