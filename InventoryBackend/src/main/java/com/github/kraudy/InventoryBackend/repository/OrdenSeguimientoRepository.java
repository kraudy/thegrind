package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.EstadosPorDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoEstadosDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;

import com.github.kraudy.InventoryBackend.model.OrdenSeguimiento;
import com.github.kraudy.InventoryBackend.model.OrdenSeguimientoPK;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
        seg.estado AS estadoActual,
        false AS permiteMover
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getFullSeguimientoByOrden(@Param("idOrden") Long idOrden);

  /* Seguimiento para impresion */

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

  /* Aqui no se especifica current date porque el id ya deberia estar filtrado */
  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual,
        CASE 
          WHEN seg.estado IN ('Normal', 'Reparacion','Impresion') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Solo permitir mover si el estado es Normal, Reparacion o Impresion
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
      AND seg.estado IN ('Normal', 'Reparacion','Impresion')  -- Solo mostrar detalles que están en estado Normal, Reparacion o Impresion
                                                              -- Por ahora estamos dejando solo los que les corresponden para impresion pero se podrian mostrar todos y marcar cuales se pueden mover a impresion
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaImpresion(@Param("idOrden") Long idOrden);

  /* Seguimiento para preparacion */

  @Query(value = """
    WITH 
    cal AS (
      SELECT
        ord.id,
        ord.id_cliente,
        CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
        ord.creada_por,
        ord.fecha_vencimiento,
        (fecha_vencimiento - current_timestamp)::text AS tiempoRestante

      FROM orden_calendario cal

      JOIN orden ord ON ord.id = cal.id_orden
      JOIN cliente cte ON cte.id = ord.id_cliente
      WHERE cal.fecha = current_date                            -- Obtenemos solo ordenes que tienen una fecha de trabajo programada para hoy
        AND ord.estado = 'Repartida'                            -- Solo considerar ordenes que están en estado Repartida
    ),
    seg AS (
      SELECT
        seg.id_orden
        
      FROM orden_seguimiento seg                                -- Relacionamos con orden_seguimiento para obtener el estado actual de cada orden
      JOIN cal ON cal.id = seg.id_orden

      WHERE seg.estado IN ('Enmarcado', 'Pegado') 
      GROUP BY seg.id_orden
    )
    SELECT
      cal.id AS id,
      cal.id_cliente AS idCliente,
      cal.clienteNombre,
      cal.creada_por AS creadaPor,
      cal.fecha_vencimiento AS fechaVencimiento,
      cal.tiempoRestante

    FROM cal
    JOIN seg ON seg.id_orden = cal.id
    ORDER BY cal.id_cliente, cal.id ASC

  """, nativeQuery = true)
  List<OrdenSeguimientoDTO> getOrdenesParaPreparacion();

  /* Aqui no se especifica current date porque el id ya deberia estar filtrado */
  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual,
        CASE 
          WHEN seg.estado IN ('Enmarcado', 'Pegado') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Solo permitir mover si el estado es Enmarcado o Pegado
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden                   -- Necesitamos el deatalle para mostrar el producto y la cantidad
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
      AND seg.estado NOT IN ('Listo', 'Entregado') -- Mostrar detalles anteriores a preparacion pero que no estén listos o entregados
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaPreparacion(@Param("idOrden") Long idOrden);

  /* Seguimiento para entrega */

  //TODO: Deberia solo mostrar el trabajo de hoy? Creo que deberia ser cualquier orden que tenga detalles listos o que este lista en si.
  // esto porque la orden puede estar lista un dia antes de su entrega
  @Query(value = """
    SELECT
      ord.id,
      ord.id_cliente AS idCliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por  AS creadaPor,
      ord.fecha_vencimiento AS fechaVencimiento,
      (fecha_vencimiento - current_timestamp)::text AS tiempoRestante

    FROM orden_calendario cal

    JOIN orden ord ON ord.id = cal.id_orden
    JOIN cliente cte ON cte.id = ord.id_cliente
    WHERE ord.estado = 'Listo'                            -- Obtenemos las ordenes que están en estado Listo de cualquier fecha, porque la orden puede estar lista un dia antes de su entrega
    ORDER BY ord.id_cliente, ord.id ASC

  """, nativeQuery = true)
  List<OrdenSeguimientoDTO> getOrdenesParaEntrega();

  /* Aqui no se especifica current date porque el id ya deberia estar filtrado */
  @Query(value = """
    SELECT
        seg.id_orden,
        seg.id_orden_detalle,
        det.id_producto,
        prod.nombre,
        det.cantidad,
        seg.tipo,
        seg.sub_tipo,
        seg.estado AS estadoActual,
        CASE 
          WHEN seg.estado IN ('Listo') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Permite mover porque los detalles en estado Listo. Una vez en entregado ya no se puede mover

    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden                   -- Necesitamos el deatalle para mostrar el producto y la cantidad
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
                                                                            -- por ahora mostramos todos los estados
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaEntrega(@Param("idOrden") Long idOrden);

  /* Monitoreo general de todas las ordenes del dia en todos los estados*/

  @Query(value = """
  WITH 
  cal AS (
    SELECT
      cal.id_orden,
      ord.id_cliente,
      CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
      ord.creada_por,
      ord.fecha_vencimiento

    FROM orden_calendario cal

    JOIN orden ord ON ord.id = cal.id_orden
    JOIN cliente cte ON cte.id = ord.id_cliente
    WHERE cal.fecha = current_date                            -- No necesita group by porque solo hay un registro por orden
      AND ord.estado = 'Repartida'                            -- Solo considerar ordenes que están en estado Repartida
  ),
  seg AS (
    SELECT
      seg.id_orden,
      seg.estado 
      
    FROM orden_seguimiento seg
    JOIN cal ON cal.id_orden = seg.id_orden

    WHERE seg.estado IN ('Normal', 'Reparacion','Impresion', 'Enmarcado', 'Pegado') 
    GROUP BY seg.id_orden, seg.estado
  ),
  normales AS (
    SELECT id_orden
    FROM seg
    WHERE estado = 'Normal'
    GROUP BY id_orden
  ),
  reparacion AS (
    SELECT id_orden
    FROM seg
    WHERE estado = 'Reparacion'
    GROUP BY id_orden
  ),
  impresion AS (
    SELECT id_orden 
    FROM seg
    WHERE estado = 'Impresion'
    GROUP BY id_orden
  ),
  enmarcado AS (
    SELECT id_orden 
    FROM seg
    WHERE estado = 'Enmarcado'
    GROUP BY id_orden
  ),
  pegado AS (
    SELECT id_orden 
    FROM seg
    WHERE estado = 'Pegado'
    GROUP BY id_orden
  )
  SELECT
    cal.id_orden,
    cal.id_cliente AS idCliente,
    cal.clienteNombre,
    cal.creada_por AS creadaPor,
    cal.fecha_vencimiento AS fechaVencimiento,
    (fecha_vencimiento - current_timestamp)::text AS tiempoRestante,
    CASE WHEN normales.id_orden IS NOT NULL THEN true ELSE false END AS tieneNormales,
    CASE WHEN reparacion.id_orden IS NOT NULL THEN true ELSE false END AS tieneReparacion,
    CASE WHEN impresion.id_orden IS NOT NULL THEN true ELSE false END AS tieneImpresion,
    CASE WHEN enmarcado.id_orden IS NOT NULL THEN true ELSE false END AS tieneEnmarcado,
    CASE WHEN pegado.id_orden IS NOT NULL THEN true ELSE false END AS tienePegado

  FROM cal

  LEFT JOIN normales ON normales.id_orden = cal.id_orden
  LEFT JOIN reparacion ON reparacion.id_orden = cal.id_orden
  LEFT JOIN impresion ON impresion.id_orden = cal.id_orden
  LEFT JOIN enmarcado ON enmarcado.id_orden = cal.id_orden
  LEFT JOIN pegado ON pegado.id_orden = cal.id_orden

  
  ORDER BY cal.id_cliente, cal.id_orden ASC
  """, nativeQuery = true)
  List<OrdenSeguimientoEstadosDTO> getOrdenesPorEstadosSeguimiento();


  @Modifying
  @Transactional
  @Query(value = """
    DELETE FROM orden_seguimiento seg
    WHERE seg.id_orden = :idOrden
    """, nativeQuery = true)
  void deleteByOrden(@Param("idOrden") Long idOrden);


  @Query(value = """
    SELECT
      seg.id_orden_detalle,
      json_agg(pte.estado ORDER BY pte.secuencia ASC)::text

    FROM orden_seguimiento seg
    JOIN producto_tipo_estado pte 
      ON pte.tipo = seg.tipo AND pte.sub_tipo = seg.sub_tipo

    WHERE seg.id_orden = :idOrden

    GROUP BY seg.id_orden_detalle
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<EstadosPorDetalleDTO> getEstadosPorDetalle(@Param("idOrden") Long idOrden);

  /* Verifica si todos los detalles de una orden están en su primer estado (secuencia = 1) */
  @Query(value = "SELECT COUNT(*) = 0 FROM orden_seguimiento WHERE id_orden = :idOrden AND secuencia > 1", nativeQuery = true)
  boolean areAllDetailsInFirstState(@Param("idOrden") Long idOrden);

  /* Verifica si todos los detalles de una orden están en estado Listo */
  @Query(value = "SELECT COUNT(*) = 0 FROM orden_seguimiento WHERE id_orden = :idOrden AND estado <> 'Listo'", nativeQuery = true)
  boolean estanTodosLosDetallesListos(@Param("idOrden") Long idOrden);

  /* Verifica si todos los detalles de una orden están en estado Entregado */
  @Query(value = "SELECT COUNT(*) = 0 FROM orden_seguimiento WHERE id_orden = :idOrden AND estado <> 'Entregado'", nativeQuery = true)
  boolean estanTodosLosDetallesEntregados(@Param("idOrden") Long idOrden);

}
