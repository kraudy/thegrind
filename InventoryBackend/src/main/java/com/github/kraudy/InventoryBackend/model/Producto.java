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

  //TODO: Tal vez cambiar la llave a compuesta de : tipo, subtipo, medida, y otro campo mas para los estilos o variaciones

  @Column(name = "tipoProducto", nullable = false, columnDefinition = "VARCHAR(25)")
  private String tipoProducto;

  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String subTipoProducto;

  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String medidaProducto;

  @Column(nullable = false, columnDefinition = "VARCHAR(25)")
  private String modeloProducto;

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
  private boolean activo = true;

  // Relaciones

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tipoProducto", referencedColumnName = "tipo", nullable = false, insertable = false, updatable = false)
  private ProductoTipo productoTipo;

  // Deleting a product will delete all its prices
  @OneToMany(mappedBy = "producto", 
              fetch = FetchType.LAZY, 
              cascade = CascadeType.ALL, 
              orphanRemoval = true)
  @JsonIgnore  // Prevents serialization cycles and infinite recursion
  private List<ProductoPrecio> precios = new ArrayList<>();

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subTipoProducto", referencedColumnName = "subTipo", nullable = false, insertable = false, updatable = false)
  private ProductoSubTipo productoSubTipo;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medidaProducto", referencedColumnName = "medida", nullable = false, insertable = false, updatable = false)
  private ProductoMedida productoMedida;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "modeloProducto", referencedColumnName = "modelo", nullable = false, insertable = false, updatable = false)
  private ProductoModelo productoModelo;
}
