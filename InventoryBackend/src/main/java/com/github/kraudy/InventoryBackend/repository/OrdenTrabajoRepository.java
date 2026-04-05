package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.UsuarioDTO;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajo;
import com.github.kraudy.InventoryBackend.model.OrdenTrabajoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, OrdenTrabajoPK> {
  @Query(value = """
    SELECT COUNT(*) = 1
    FROM orden_trabajo trabajo
    WHERE trabajo.id_orden = :idOrden
      AND trabajo.id_orden_detalle = :idOrdenDetalle
      AND trabajo.estado = :estado
    """, nativeQuery = true)
  boolean detalleEstaAsignado(Long idOrden, Long idOrdenDetalle, String estado);

  @Query(value = """
    SELECT
        trabajo.trabajador AS usuario

    FROM orden_trabajo trabajo
    WHERE trabajo.id_orden = :idOrden
      AND trabajo.id_orden_detalle = :idOrdenDetalle
      AND trabajo.estado = 'Reparacion'
    """, nativeQuery = true)
  UsuarioDTO getReparador(Long idOrden, Long idOrdenDetalle);

  @Query(value = """
    SELECT
        trabajo.trabajador AS usuario

    FROM orden_trabajo trabajo
    WHERE trabajo.id_orden = :idOrden
      AND trabajo.id_orden_detalle = :idOrdenDetalle
      AND trabajo.estado = 'Normal'
    """, nativeQuery = true)
  UsuarioDTO getNormal(Long idOrden, Long idOrdenDetalle);

  @Modifying
  @Transactional
  @Query(value = """
    DELETE FROM orden_trabajo trabajo
    WHERE trabajo.id_orden = :idOrden
    """, nativeQuery = true)
  void deleteByOrden(@Param("idOrden") Long idOrden);

}
