package com.github.kraudy.InventoryBackend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Factura")
public class Factura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_cliente", nullable = false)
  private Long idCliente;

  @Column(name = "id_orden", nullable = false)
  private Long idOrden;

  @Column(nullable = false, columnDefinition = "NUMERIC(12,4)")
  private BigDecimal total;

  @Column(name = "usuario_creacion", nullable = false, columnDefinition = "VARCHAR(50)")
  private String usuarioCreacion;

  @CreationTimestamp  
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;

  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String estado; // Pagada, Anulada, Parcial, Pendiente?

  // ================== Relaciones ==================
  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_orden", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  private Orden orden;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  private Cliente cliente;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_creacion", referencedColumnName = "usuario", nullable = false, insertable = false, updatable = false)
  private Usuario usuario;

}
