package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/* This thing maps product */
public interface ProductRepository extends JpaRepository<Product, Long> {}