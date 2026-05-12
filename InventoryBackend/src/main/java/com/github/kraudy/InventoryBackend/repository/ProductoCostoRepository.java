package com.github.kraudy.InventoryBackend.repository;


import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoCostoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoCostoRepository extends JpaRepository<ProductoCosto, ProductoCostoPK> {
  @Query("""
      SELECT p FROM ProductoCosto p
      WHERE p.productoId = :productoId AND p.activo = true
      ORDER BY p.costo DESC
      """)
    List<ProductoCosto> getAllProductoCostos(@Param("productoId") Long productoId);

    /**
     * Bulk fetch of all active costos for the given product ids; used by the
     * catalog list endpoint to embed cost info per row without N+1 queries.
     */
    @Query("""
      SELECT p FROM ProductoCosto p
      WHERE p.activo = true AND p.productoId IN :productoIds
      ORDER BY p.productoId, p.tipoCosto
      """)
    List<ProductoCosto> findActiveByProductoIdIn(@Param("productoIds") List<Long> productoIds);
}

