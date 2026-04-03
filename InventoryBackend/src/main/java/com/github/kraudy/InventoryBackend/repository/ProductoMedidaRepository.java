package com.github.kraudy.InventoryBackend.repository;


import com.github.kraudy.InventoryBackend.model.ProductoMedida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoMedidaRepository extends JpaRepository<ProductoMedida, String> {}