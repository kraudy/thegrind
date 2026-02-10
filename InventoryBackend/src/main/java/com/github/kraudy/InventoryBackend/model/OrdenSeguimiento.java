package com.github.kraudy.InventoryBackend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

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

  @Id
  @Column(name = "id_producto")
  private Long idProducto;

  @Id
  @Column(name = "estado", nullable = false, length = 100)
  private String estado;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "id_orden", referencedColumnName = "id_orden", insertable = false, updatable = false),
      @JoinColumn(name = "id_orden_detalle", referencedColumnName = "id_orden_detalle", insertable = false, updatable = false),
      @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", insertable = false, updatable = false)
  })
  private OrdenDetalle ordenDetalle;

  // other fields...
}