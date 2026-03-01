package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistorico;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoHistoricoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface OrdenSeguimientoHistoricoRepository extends JpaRepository<OrdenSeguimientoHistorico, OrdenSeguimientoHistoricoPK> {
  
  @Modifying
  @Query(value = """
    DELETE FROM orden_seguimiento_historico hist
    WHERE hist.id_orden = :idOrden
    """, nativeQuery = true)
  void deleteByOrden(@Param("idOrden") Long idOrden);
}