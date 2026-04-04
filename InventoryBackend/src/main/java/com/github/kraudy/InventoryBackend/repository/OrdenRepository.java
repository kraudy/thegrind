package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;
import com.github.kraudy.InventoryBackend.model.Orden;
import com.github.kraudy.InventoryBackend.model.Producto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
  @Query(value = """
    SELECT
      ord.id AS id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por AS creadaPor,
      ord.total_monto AS totalMonto,
      ord.total_productos AS totalProductos,
      ord.fecha_creacion AS fechaCreacion,
      ord.fecha_vencimiento AS fechaVencimiento,
      ord.fecha_preparada AS fechaPreparada,
      ord.fecha_despachada AS fechaDespachada,
      ord.fecha_modificacion AS fechaModificacion,
      ord.estado AS estado
    FROM orden ord
    JOIN cliente cte ON cte.id = ord.id_cliente
    ORDER BY ord.id ASC
    """, nativeQuery = true)
  List<OrdenDTO> getAll();


  @Query(value = """
    SELECT
      ord.id AS id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por AS creadaPor,
      ord.total_monto AS totalMonto,
      ord.total_productos AS totalProductos,
      ord.fecha_creacion AS fechaCreacion,
      ord.fecha_vencimiento AS fechaVencimiento,
      ord.fecha_preparada AS fechaPreparada,
      ord.fecha_despachada AS fechaDespachada,
      ord.fecha_modificacion AS fechaModificacion,
      ord.estado AS estado
    FROM orden ord
    JOIN cliente cte ON cte.id = ord.id_cliente
    WHERE ord.id = :id
    """, nativeQuery = true)
  OrdenDTO getOrdenById(Long id);

  //TODO: Move this to a before insert,update, delete trigger in the database
  @Modifying
  @Transactional  // Required for DML operations
  @Query(value = """
    UPDATE orden
    SET 
        total_monto = (SELECT COALESCE(SUM(subtotal), 0) FROM orden_detalle WHERE id_orden = :idOrden), 
        total_productos = (SELECT COALESCE(SUM(cantidad), 0) FROM orden_detalle WHERE id_orden = :idOrden)
    WHERE id = :idOrden
  """, nativeQuery = true)
  void updateOrdenTotales(
      Long idOrden
  );

  @Modifying
  @Transactional
  @Query(value = """
    UPDATE orden
    SET estado = :estado
    WHERE id = :idOrden
    """, nativeQuery = true)
  void updateEstado(Long idOrden, String estado);

  @Query(value = """
    SELECT COALESCE(SUM(od.subtotal), 0)
    FROM orden_detalle od
    WHERE od.id_orden = :idOrden
    """, nativeQuery = true)
  BigDecimal calculateTotalMonto(Long idOrden);

  @Query(value = """
    SELECT COALESCE(SUM(od.cantidad), 0)
    FROM orden_detalle od
    WHERE od.id_orden = :idOrden
    """, nativeQuery = true)
  int calculateTotalProductos(Long idOrden);

  @Query(value = """
    SELECT
      ord.id AS id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por AS creadaPor,
      ord.total_monto AS totalMonto,
      ord.total_productos AS totalProductos,
      ord.fecha_creacion AS fechaCreacion,
      ord.fecha_vencimiento AS fechaVencimiento,
      ord.fecha_preparada AS fechaPreparada,
      ord.fecha_despachada AS fechaDespachada,
      ord.fecha_modificacion AS fechaModificacion,
      ord.estado AS estado
    FROM orden ord
    JOIN cliente cte ON cte.id = ord.id_cliente
    WHERE ord.estado = 'Recibida'
    ORDER BY cte.id, ord.fecha_vencimiento ASC   --ord.id ASC
    """, nativeQuery = true)
  List<OrdenDTO> getRecibidas();

  @Query(value ="""
    SELECT 
      ord.id AS id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por AS creadaPor,
      ord.total_monto AS totalMonto,
      ord.total_productos AS totalProductos,
      ord.fecha_creacion AS fechaCreacion,
      ord.fecha_vencimiento AS fechaVencimiento,
      ord.fecha_preparada AS fechaPreparada,
      ord.fecha_despachada AS fechaDespachada,
      ord.fecha_modificacion AS fechaModificacion,
      ord.estado AS estado

    FROM orden ord
    JOIN cliente cte ON cte.id = ord.id_cliente
    WHERE (:id IS NULL OR ord.id = :id)
      AND (:cliente IS NULL OR LOWER(CONCAT(cte.nombre, ' ', cte.apellido)) LIKE LOWER(CONCAT('%', :cliente, '%')))
      AND (:recepcionista IS NULL OR LOWER(ord.creada_por) LIKE LOWER(CONCAT('%', :recepcionista, '%')))
      AND (:estado IS NULL OR ord.estado = :estado)
    ORDER BY ord.id
    """, nativeQuery = true)
  List<OrdenDTO> obtenerOrdenes(@Param("id") Long id, @Param("cliente") String cliente, @Param("recepcionista") String recepcionista, @Param("estado") String estado);
}
