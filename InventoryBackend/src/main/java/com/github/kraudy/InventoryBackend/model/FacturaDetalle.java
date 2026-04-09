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
@IdClass(FacturaDetallePK.class)
@Table(name = "FacturaDetalle")
public class FacturaDetalle {
  @Id
  @Column(name = "id_factura", insertable = false, updatable = false)
  private Long idFactura;

  @Id
  @Column(name = "id_detalle", nullable = false)
  private Long idDetalle;

  @Column(name = "id_orden_detalle", nullable = false)
  private Long idOrdenDetalle;

  @Column(name = "id_producto", nullable = false, insertable = false, updatable = false)
  private Long idProducto;

  @Column(nullable = false, columnDefinition = "DECIMAL(12,4)")
  private BigDecimal precio;

  @Column(nullable = false, columnDefinition = "INTEGER")
  private int cantidad;

  @Column(nullable = false, columnDefinition = "DECIMAL(12,4)")
  private BigDecimal subtotal;

  @Column(name = "usuario_creacion", nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuarioCreacion;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private LocalDateTime fechaCreacion;

  // ================== Relaciones ==================
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_factura", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  private Factura factura;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_producto", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  private Producto producto;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_creacion", referencedColumnName = "usuario", nullable = false, insertable = false, updatable = false)
  private Usuario usuario;

}
