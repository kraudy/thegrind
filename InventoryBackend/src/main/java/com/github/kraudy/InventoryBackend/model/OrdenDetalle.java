package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@IdClass(OrdenDetallePK.class)
@Table(name = "OrdenDetalle")
public class OrdenDetalle {
  // === NEW: simple ID fields that match the IdClass ===
  @Id
  @Column(name = "id_orden", insertable = false, updatable = false)
  private Long idOrden;

  @Id
  @Column(name = "id_producto", insertable = false, updatable = false)
  private Long idProducto;

  @Id
  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  // === Associations with @MapsId (derive the ID values from them) ===
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_orden", nullable = false)  // remove insertable=false, updatable=false
  private Orden orden;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_producto", nullable = false)  // remove insertable=false, updatable=false
  private Producto producto;

  // === Rest of your fields (cantidad, precioUnitario, etc.) ===
  private int cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal subtotal;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaModificacion;

  @OneToMany(mappedBy = "ordenDetalle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrdenSeguimiento> seguimientos = new ArrayList<>();
}
