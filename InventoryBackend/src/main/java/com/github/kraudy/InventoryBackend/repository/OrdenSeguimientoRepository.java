package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.EstadosPorDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoEstadosGeneralDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleEntregaDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetalleImpresionDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenSeguimientoDetallePreparacionDTO;
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
        trabajo.trabajador AS trabajador,
        trabajo.cantidad_trabajada AS cantidadTrabajada,
        det.cantidad - COALESCE(trabajo.cantidad_trabajada, 0) AS cantidadPendiente,
        CASE 
          WHEN seg.estado IN ('Normal', 'Reparacion', 'Impresion') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Solo permitir mover si el estado es Normal, Reparacion o Impresion
    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    -- Lo tenia en INNER pero lo deje en LEFT, el avance de seguimiento deberia asegurarlo
    LEFT JOIN orden_trabajo trabajo ON trabajo.id_orden = seg.id_orden
                        AND trabajo.id_orden_detalle = seg.id_orden_detalle
                        AND trabajo.estado IN ('Normal', 'Reparacion') -- Solo considerar detalles que están asignados a un trabajador en estado Normal o Reparacion

    WHERE seg.id_orden = :idOrden
      AND seg.estado IN ('Normal', 'Reparacion','Impresion')  -- Solo mostrar detalles que están en estado Normal, Reparacion o Impresion
                                                              -- Por ahora estamos dejando solo los que les corresponden para impresion pero se podrian mostrar todos y marcar cuales se pueden mover a impresion
      AND seg.sub_tipo IN ('Normal', 'Reparacion') -- Solo mostrar detalles que son de tipo Normal o Reparacion porque son los unicos que se imprimen
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleImpresionDTO> getSeguimientoDeOrdenParaImpresion(@Param("idOrden") Long idOrden);

  /* Seguimiento para repartir */

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
      WHERE ord.estado = 'Repartida'                            -- Solo considerar ordenes que están en estado Repartida
            -- No filtramos por fecha porque se pueden repartir las ordenes por adelantado
    ),
    seg AS (
      SELECT
        seg.id_orden
        
      FROM orden_seguimiento seg                                -- Relacionamos con orden_seguimiento para obtener el estado actual de cada orden
      JOIN cal ON cal.id = seg.id_orden

      WHERE seg.estado IN ('Repartida') 
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
  List<OrdenSeguimientoDTO> getOrdenesParaRepartir();

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
          WHEN seg.estado IN ('Repartida') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Solo permitir mover si el estado es Repartida

    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden                   -- Necesitamos el deatalle para mostrar el producto y la cantidad
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto
    WHERE seg.id_orden = :idOrden
      AND seg.estado IN ('Repartida', 'Normal', 'Reparacion') -- Mostrar detalles en estado Repartida, Normal o Reparacion para dar visibilidad de lo que se tiene que repartir pero solo permitir mover los que están en Repartida
      AND seg.sub_tipo IN ('Normal', 'Reparacion') -- Solo mostrar detalles que son de tipo Normal o Reparacion
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleDTO> getSeguimientoDeOrdenParaRepartir(@Param("idOrden") Long idOrden);

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
    ORDER BY cal.id_cliente, cal.fecha_vencimiento ASC

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
        -- Aqui usar el estadoActual que debe referirse a Enmarcado o Pegado.
        COALESCE(trabajoActual.trabajador, '') AS trabajadorActual,
        COALESCE(trabajoActual.cantidad_asignada, 0) AS cantidadAsignadaActual,
        COALESCE(trabajoActual.cantidad_trabajada, 0) AS cantidadTrabajadaActual,

        COALESCE(trabajoPrevio.estado, '') AS estadoPrevio,
        COALESCE(trabajoPrevio.trabajador, '') AS trabajadorPrevio,
        COALESCE(trabajoPrevio.cantidad_asignada, 0) AS cantidadAsignadaPrevio,
        COALESCE(trabajoPrevio.cantidad_trabajada, 0) AS cantidadTrabajadaPrevio,

        CASE 
          WHEN seg.estado IN ('Enmarcado', 'Pegado') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Solo permitir mover si el estado es Enmarcado o Pegado

    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden                   -- Necesitamos el deatalle para mostrar el producto y la cantidad
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto

    LEFT JOIN orden_trabajo trabajoPrevio 
                         ON trabajoPrevio.id_orden = seg.id_orden
                        AND trabajoPrevio.id_orden_detalle = seg.id_orden_detalle
                        AND trabajoPrevio.estado IN ('Normal', 'Reparacion') -- Lo ocupamos para obtener la cantidad trabajada previamente

    LEFT JOIN orden_trabajo trabajoActual 
                         ON trabajoActual.id_orden = seg.id_orden
                        AND trabajoActual.id_orden_detalle = seg.id_orden_detalle
                        AND trabajoActual.estado IN ('Pegado', 'Enmarcado') -- Lo ocupamos para obtener la cantidad trabajada actualmente

    WHERE seg.id_orden = :idOrden
      AND seg.estado NOT IN ('Listo', 'Entregado') -- Mostrar detalles anteriores a preparacion pero que no estén listos o entregados
    ORDER BY seg.id_orden_detalle ASC 
    """, nativeQuery = true)
  List<OrdenSeguimientoDetallePreparacionDTO> getSeguimientoDeOrdenParaPreparacion(@Param("idOrden") Long idOrden);

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
    ORDER BY ord.id_cliente, ord.fecha_vencimiento ASC

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
        -- Aqui usar el estadoActual es Listo.
        COALESCE(trabajoActual.trabajador, '') AS trabajadorActual,
        COALESCE(trabajoActual.cantidad_asignada, 0) AS cantidadAsignadaActual,
        COALESCE(trabajoActual.cantidad_trabajada, 0) AS cantidadTrabajadaActual,

        COALESCE(trabajoPrevio.estado, '') AS estadoPrevio,
        COALESCE(trabajoPrevio.trabajador, '') AS trabajadorPrevio,
        COALESCE(trabajoPrevio.cantidad_asignada, 0) AS cantidadAsignadaPrevio,
        COALESCE(trabajoPrevio.cantidad_trabajada, 0) AS cantidadTrabajadaPrevio,

        CASE 
          WHEN seg.estado IN ('Listo') THEN true 
          ELSE false 
        END AS permiteMover                                                 -- Permite mover porque los detalles en estado Listo. Una vez en entregado ya no se puede mover

    FROM orden_seguimiento seg
    JOIN orden_detalle det ON det.id_orden = seg.id_orden                   -- Necesitamos el deatalle para mostrar el producto y la cantidad
                        AND det.id_orden_detalle = seg.id_orden_detalle
    JOIN producto prod ON prod.id = det.id_producto

    LEFT JOIN orden_trabajo trabajoPrevio 
                         ON trabajoPrevio.id_orden = seg.id_orden
                        AND trabajoPrevio.id_orden_detalle = seg.id_orden_detalle
                        AND trabajoPrevio.estado IN ('Normal', 'Reparacion') -- Lo ocupamos para obtener la cantidad trabajada previamente

    LEFT JOIN orden_trabajo trabajoActual 
                         ON trabajoActual.id_orden = seg.id_orden
                        AND trabajoActual.id_orden_detalle = seg.id_orden_detalle
                        AND trabajoActual.estado IN ('Pegado', 'Enmarcado') -- Lo ocupamos para obtener la cantidad trabajada actualmente

    WHERE seg.id_orden = :idOrden
                                                                            -- por ahora mostramos todos los estados
    ORDER BY seg.id_orden_detalle ASC
    """, nativeQuery = true)
  List<OrdenSeguimientoDetalleEntregaDTO> getSeguimientoDeOrdenParaEntrega(@Param("idOrden") Long idOrden);

  /* Monitoreo general de todas las ordenes del dia en todos los estados*/

  //TODO: Valorar obtener el detalle en cada CTE de forma que en el FE se pueda mostrar a la izquierda la informacion de la orden y a la derecha, tarjetas
  // de los detalles en sus dieferentes estados y que al darle click pueda mostrar la vista correspondiente
  //TODO: Lo otro, es que no se si necesito hacer todo esto porque orden_seguimiento ya tiene toda la informacion y solo toma en cuenta ordenes ya repartidas
  @Query(value = """
    WITH seg AS (
      SELECT 
        seg.id_orden,
        seg.estado,
        COUNT(*) AS detalle_count
      FROM orden_seguimiento seg
      WHERE seg.estado IN ('Repartida', 'Normal', 'Reparacion', 'Impresion', 
                          'Enmarcado', 'Pegado', 'Listo', 'Entregado')
      GROUP BY seg.id_orden, seg.estado
    ),
    cal AS (
      SELECT 
        ord.id as id_orden,
        ord.id_cliente,
        CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
        ord.estado as estadoOrden,
        ord.creada_por,
        ord.fecha_vencimiento,
        (ord.fecha_vencimiento - CURRENT_TIMESTAMP)::text AS tiempoRestante
      FROM orden ord
      JOIN cliente cte ON cte.id = ord.id_cliente
      LEFT JOIN orden_calendario cal ON ord.id = cal.id_orden
      WHERE ord.estado != 'Entregado'          -- Only active orders
    )
    SELECT
      cal.id_orden,
      cal.id_cliente AS idCliente,
      cal.clienteNombre,
      cal.estadoOrden,
      cal.creada_por AS creadaPor,
      cal.fecha_vencimiento AS fechaVencimiento,
      cal.tiempoRestante,

      -- Boolean flags (for quick UI badges)
      CASE WHEN repartidas.id_orden IS NOT NULL THEN true ELSE false END AS tieneRepartidas,
      CASE WHEN normales.id_orden IS NOT NULL THEN true ELSE false END AS tieneNormales,
      CASE WHEN reparacion.id_orden IS NOT NULL THEN true ELSE false END AS tieneReparacion,
      CASE WHEN impresion.id_orden IS NOT NULL THEN true ELSE false END AS tieneImpresion,
      CASE WHEN enmarcado.id_orden IS NOT NULL THEN true ELSE false END AS tieneEnmarcado,
      CASE WHEN pegado.id_orden IS NOT NULL THEN true ELSE false END AS tienePegado,
      CASE WHEN listo.id_orden IS NOT NULL THEN true ELSE false END AS tieneListo,
      CASE WHEN entregado.id_orden IS NOT NULL THEN true ELSE false END AS tieneEntregado,

      -- Counts (very useful for the UI)
      COALESCE(repartidas.detalle_count, 0) AS countRepartidas,
      COALESCE(normales.detalle_count, 0)   AS countNormales,
      COALESCE(reparacion.detalle_count, 0) AS countReparacion,
      COALESCE(impresion.detalle_count, 0)  AS countImpresion,
      COALESCE(enmarcado.detalle_count, 0)  AS countEnmarcado,
      COALESCE(pegado.detalle_count, 0)     AS countPegado,
      COALESCE(listo.detalle_count, 0)      AS countListo,
      COALESCE(entregado.detalle_count, 0)  AS countEntregado

    FROM cal
    LEFT JOIN seg repartidas  ON repartidas.id_orden  = cal.id_orden AND repartidas.estado  = 'Repartida'
    LEFT JOIN seg normales    ON normales.id_orden    = cal.id_orden AND normales.estado    = 'Normal'
    LEFT JOIN seg reparacion  ON reparacion.id_orden  = cal.id_orden AND reparacion.estado  = 'Reparacion'
    LEFT JOIN seg impresion   ON impresion.id_orden   = cal.id_orden AND impresion.estado   = 'Impresion'
    LEFT JOIN seg enmarcado   ON enmarcado.id_orden   = cal.id_orden AND enmarcado.estado   = 'Enmarcado'
    LEFT JOIN seg pegado      ON pegado.id_orden      = cal.id_orden AND pegado.estado      = 'Pegado'
    LEFT JOIN seg listo       ON listo.id_orden       = cal.id_orden AND listo.estado       = 'Listo'
    LEFT JOIN seg entregado   ON entregado.id_orden   = cal.id_orden AND entregado.estado   = 'Entregado'

    ORDER BY cal.id_cliente, cal.fecha_vencimiento ASC
  """, nativeQuery = true)
  List<OrdenSeguimientoEstadosGeneralDTO> getOrdenesPorEstadosSeguimiento();


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
      json_agg(pte.estado ORDER BY pte.secuencia ASC)::text,
      seg.estado AS estadoActual

    FROM orden_seguimiento seg
    JOIN producto_tipo_estado pte 
      ON pte.tipo = seg.tipo AND pte.sub_tipo = seg.sub_tipo

    WHERE seg.id_orden = :idOrden

    GROUP BY seg.id_orden_detalle, seg.estado
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
