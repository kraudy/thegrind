package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.ProductoModelo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoModeloRepository extends JpaRepository<ProductoModelo, String> {}