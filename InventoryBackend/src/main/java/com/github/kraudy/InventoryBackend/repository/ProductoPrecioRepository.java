package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.ProductoPrecioDTO;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoPrecioRepository extends JpaRepository<ProductoPrecio, ProductoPrecioPK> {
    @Query("""
      SELECT p FROM ProductoPrecio p
      WHERE p.productoId = :productoId AND p.activo = true
      ORDER BY p.precio DESC
      """)
    List<ProductoPrecio> getAllProductoPrecios(Long productoId);

    @Query("""
      SELECT new com.github.kraudy.InventoryBackend.dto.ProductoPrecioDTO(
          pp.productoId, pp.precio, pp.descripcion, pp.cantidadRequerida,
          pp.fechaCreacion, pp.fechaModificacion, pp.activo,
          prod.nombre, prod.tipoProducto
      )
      FROM ProductoPrecio pp
      JOIN pp.producto prod
      WHERE pp.activo = true
      ORDER BY prod.nombre, pp.precio DESC
      """)
    List<ProductoPrecioDTO> getAll();
}