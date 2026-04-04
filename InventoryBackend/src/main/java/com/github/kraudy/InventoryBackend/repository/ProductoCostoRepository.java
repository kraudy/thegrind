package com.github.kraudy.InventoryBackend.repository;


import com.github.kraudy.InventoryBackend.model.ProductoCosto;
import com.github.kraudy.InventoryBackend.model.ProductoCostoPK;
import com.github.kraudy.InventoryBackend.model.ProductoPrecio;

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
}

