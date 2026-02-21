package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenSeguimientoRepository extends JpaRepository<OrdenSeguimiento, OrdenSeguimientoPK> {}
