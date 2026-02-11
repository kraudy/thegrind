package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.ProductoPrecio;
import com.github.kraudy.InventoryBackend.model.ProductoPrecioPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoPrecioRepository extends JpaRepository<ProductoPrecio, ProductoPrecioPK> {
   // Get all prices for a specific product, ordered by creation date (newest first)
    List<ProductoPrecio> findByProductoIdOrderByFechaCreacionDesc(Long productoId);

    // Optional convenience method: only active prices
    List<ProductoPrecio> findByProductoIdAndActivoTrueOrderByFechaCreacionDesc(Long productoId);
}