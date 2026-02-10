package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/* This thing maps product */
public interface ProductoRepository extends JpaRepository<Producto, Long> {}