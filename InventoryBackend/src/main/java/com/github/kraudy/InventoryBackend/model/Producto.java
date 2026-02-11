package com.github.kraudy.InventoryBackend.model;

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
@Table(name = "Producto") 
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tipoProducto", nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipoProducto;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tipoProducto", referencedColumnName = "tipo", nullable = false, insertable = false, updatable = false)
  private ProductoTipo productoTipo;

  @Column(nullable = false, columnDefinition = "VARCHAR(100)")
  private String nombre;

  @Column(nullable = false, columnDefinition = "VARCHAR(255)")
  private String descripcion;

  @CreationTimestamp  
  @Column(updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaCreacion;
 
  @UpdateTimestamp
  @Column(nullable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime fechaModificacion;

  @Column(nullable = false)
  private boolean activo;

  // === One-to-many relationship with ProductoPrecio ===
  // Deleting a product will delete all its prices
  @OneToMany(mappedBy = "producto", 
              fetch = FetchType.LAZY, 
              cascade = CascadeType.ALL, 
              orphanRemoval = true)
  @JsonIgnore  // Prevents serialization cycles and infinite recursion
  private List<ProductoPrecio> precios = new ArrayList<>();
}
