package com.github.kraudy.InventoryBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.kraudy.InventoryBackend.dto.FacturaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.model.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

  @Query(value ="""
    SELECT 
      fact.id AS id,
      fact.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      fact.id_orden AS idOrden,
      fact.usuario_creacion AS usuarioCreacion,
      fact.total AS total,
      fact.fecha_creacion AS fechaCreacion,
      fact.estado AS estado

    FROM factura fact

    JOIN cliente cte ON cte.id = fact.id_cliente

    WHERE (:id IS NULL OR fact.id = :id)
      AND (:cliente IS NULL OR LOWER(CONCAT(cte.nombre, ' ', cte.apellido)) LIKE LOWER(CONCAT('%', :cliente, '%')))
      AND (:facturador IS NULL OR LOWER(fact.usuario_creacion) LIKE LOWER(CONCAT('%', :facturador, '%')))
      AND (:estado IS NULL OR fact.estado = :estado)

    ORDER BY fact.id
    """, nativeQuery = true)
  List<FacturaDTO> obtenerFacturas(@Param("id") Long id, @Param("cliente") String cliente, @Param("facturador") String facturador, @Param("estado") String estado);

  @Query(value = """
    SELECT 
      fact.id AS id,
      fact.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      fact.id_orden AS idOrden,
      fact.usuario_creacion AS usuarioCreacion,
      fact.total AS total,
      fact.fecha_creacion AS fechaCreacion,
      fact.estado AS estado

    FROM factura fact

    JOIN cliente cte ON cte.id = fact.id_cliente

    WHERE fact.id = :id
    """, nativeQuery = true)
  FacturaDTO getFacturaById(@Param("id") Long id);

}
