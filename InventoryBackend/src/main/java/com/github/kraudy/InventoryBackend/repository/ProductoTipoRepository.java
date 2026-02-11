package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.ProductoTipo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoTipoRepository extends JpaRepository<ProductoTipo, String> {}