package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {}
