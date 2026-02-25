package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistorico;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistoricoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenSeguimientoHistoricoRepository extends JpaRepository<OrdenSeguimientoHistorico, OrdenSeguimientoHistoricoPK> {
  
}