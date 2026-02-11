package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {}
