package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.ProductoPrecioDTO;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value ="""
      SELECT producto_id, precio, descripcion, cantidad_requerida, fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion, activo
      FROM producto_precio pp
      WHERE pp.producto_id = :idProducto
      """, nativeQuery = true)
    List<ProductoPrecio> obtenerPreciosProducto(@Param("idProducto") Long idProducto);

    /**
     * Bulk fetch of all active precios for the given product ids; used by the
     * catalog list endpoint to embed pricing per row without N+1 queries.
     */
    @Query("""
      SELECT p FROM ProductoPrecio p
      WHERE p.activo = true AND p.productoId IN :productoIds
      ORDER BY p.productoId, p.precio DESC
      """)
    List<ProductoPrecio> findActiveByProductoIdIn(@Param("productoIds") List<Long> productoIds);
}