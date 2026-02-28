package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenSeguimientoRepository extends JpaRepository<OrdenSeguimiento, OrdenSeguimientoPK> {
  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle ORDER BY s.fechaCreacion ASC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionAsc(Long idOrden, Long idOrdenDetalle);

  @Query("SELECT s FROM OrdenSeguimiento s WHERE s.idOrden = :idOrden AND s.idOrdenDetalle = :idOrdenDetalle ORDER BY s.fechaCreacion DESC")
  List<OrdenSeguimiento> findByDetalleOrderByFechaCreacionDesc(Long idOrden, Long idOrdenDetalle);

  List<OrdenSeguimiento> findByIdOrden(Long idOrden);

  /* Muestra el seguimiento de todos los detalles de una orden */
  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getFullSeguimientoByOrden(@Param("idOrden") Long idOrden);

  @Query(value = """
    WITH seg AS (
      SELECT
        seg.id_orden
      FROM orden_seguimiento seg
      WHERE seg.estado IN ('Normal', 'Reparacion','Impresion')
      GROUP BY seg.id_orden
    )
    SELECT
      ord.id AS id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por AS creadaPor,
      ord.fecha_vencimiento AS fechaVencimiento,
      (fecha_vencimiento - current_timestamp)::text AS tiempoRestante

    FROM orden ord
    JOIN cliente cte ON cte.id = ord.id_cliente
    JOIN seg ON seg.id_orden = ord.id
    JOIN orden_calendario cal ON cal.id_orden = ord.id
    WHERE ord.estado = 'Repartida'                               -- Solo mostrar ordenes que están en estado Repartida
      AND cal.fecha = current_date                               -- Solo mostrar ordenes que tienen una fecha de trabajo programada para hoy
    ORDER BY ord.id_cliente, ord.id ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDTO> getOrdenesParaImpresion();

  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
      AND seg.estado IN ('Normal', 'Reparacion','Impresion')  -- Solo mostrar detalles que están en estado Normal, Reparacion o Impresion
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaImpresion(@Param("idOrden") Long idOrden);
}
