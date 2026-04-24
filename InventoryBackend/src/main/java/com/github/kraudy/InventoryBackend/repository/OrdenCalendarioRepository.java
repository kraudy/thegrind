package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenCalendario.EstadisticasDistribucionHoyDTO;
import com.github.kraudy.InventoryBackend.dto.OrdenCalendario.TrabajadorCargaDTO;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenCalendarioRepository extends JpaRepository<OrdenCalendario, Long> {

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        cal.fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
        CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
        ord.fecha_vencimiento,
        cal.fecha_creacion,
        cal.usuario_creacion,
        cal.fecha_modificacion,
        cal.usuario_modificacion
        
    FROM orden_calendario cal
    JOIN orden ord on (ord.id = cal.id_orden)
    JOIN cliente cte on (cte.id = ord.id_cliente)
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  List<OrdenCalendarioDTO> getAllOrdenCalendario();

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        cal.fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
        CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
        ord.fecha_vencimiento,
        cal.fecha_creacion,
        cal.usuario_creacion,
        cal.fecha_modificacion,
        cal.usuario_modificacion
        
    FROM orden_calendario cal
    JOIN orden ord on (ord.id = cal.id_orden)
    JOIN cliente cte on (cte.id = ord.id_cliente)

    WHERE cal.id_orden = :idOrden
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  OrdenCalendarioDTO getByIdOrdenCalendario(Long idOrden);

  @Query(value = """
      SELECT
          cal.id_orden,
          cal.fecha_trabajo,
          cal.fecha,
          EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
          EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
          EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
          cal.fecha_creacion,
          cal.usuario_creacion,
          cal.fecha_modificacion,
          cal.usuario_modificacion

      FROM orden_calendario cal
      WHERE cal.fecha = TO_DATE(:fecha, 'YYYY-MM-DD')
      ORDER BY cal.fecha_trabajo ASC
  """, nativeQuery = true)
  List<OrdenCalendarioDTO> getByFecha(String fecha);

  @Query(value = """
      SELECT Count(*)
      FROM orden_calendario cal
      WHERE cal.fecha = TO_DATE(:fecha, 'YYYY-MM-DD')
      GROUP BY cal.fecha
  """, nativeQuery = true)
  Long getTotalPorFecha(String fecha);

  // Genera calendario de fechas de la semana actual y la siguiente
  @Query(value = """
      WITH days AS (
        SELECT 
          d::date AS date,
          to_char(d, 'FMDay') AS day_name,
          CASE WHEN d::date < CURRENT_DATE THEN 'past' WHEN d::date = CURRENT_DATE THEN 'today' ELSE 'future' END AS relative_to_today,
          CASE WHEN d::date < date_trunc('week', CURRENT_DATE)::date + 7 THEN 'this week' ELSE 'next week' END AS week_label
        FROM generate_series(
          date_trunc('week', CURRENT_DATE)::date,
          date_trunc('week', CURRENT_DATE)::date + 12,
          '1 day'::interval
        ) AS d
        WHERE EXTRACT(ISODOW FROM d) BETWEEN 1 AND 6
      )
      SELECT 
        days.date,
        days.day_name,
        days.relative_to_today,
        days.week_label,
        COALESCE(COUNT(oc.id_orden), 0) AS order_count
      FROM days
      LEFT JOIN orden_calendario oc ON DATE(oc.fecha_trabajo) = days.date
      GROUP BY days.date, days.day_name, days.relative_to_today, days.week_label
      ORDER BY days.date
      """, nativeQuery = true)
  List<Object[]> getCalendarDaysWithCountsRaw();

  // Obtiene todas las ordenes de la semana actual y la siguiente
  @Query(value = """
      SELECT
          cal.id_orden,
          cal.fecha_trabajo,
          cal.fecha,
          EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
          EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
          EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
          CONCAT(cte.nombre, ' ', cte.apellido) AS clienteNombre,
          ord.fecha_vencimiento,
          cal.fecha_creacion,
          cal.usuario_creacion,
          cal.fecha_modificacion,
          cal.usuario_modificacion
      FROM orden_calendario cal
      JOIN orden ord on (ord.id = cal.id_orden)
      JOIN cliente cte on (cte.id = ord.id_cliente)
      WHERE cal.fecha >= date_trunc('week', CURRENT_DATE)::date 
        AND cal.fecha <= date_trunc('week', CURRENT_DATE)::date + 13
      ORDER BY cal.fecha_trabajo ASC
      """, nativeQuery = true)
  List<OrdenCalendarioDTO> getOrdersInTwoWeeks();

  List<OrdenCalendario> findByFechaBefore(LocalDate fecha);

  @Query(value = """
      SELECT COUNT(*) 
      FROM orden 
      WHERE estado = 'Recibida'
      """, nativeQuery = true)
  long countOrdenesRecibidas();

  @Query(value = """
      SELECT 
          trabajador,
          COUNT(*)::bigint AS "cantidadDetalles"
      FROM orden_trabajo

      INNER JOIN orden_calendario cal 
          ON (orden_trabajo.id_orden = cal.id_orden AND 
              cal.fecha = CURRENT_DATE)

      INNER JOIN orden_seguimiento seg 
          ON (orden_trabajo.id_orden = seg.id_orden AND 
              orden_trabajo.id_orden_detalle = seg.id_orden_detalle)

      WHERE seg.estado = 'Reparacion'
        
      GROUP BY trabajador
      ORDER BY "cantidadDetalles" DESC
      """, nativeQuery = true)
  List<TrabajadorCargaDTO> getReparadoresHoy();

  @Query(value = """
      SELECT 
          trabajador,
          COUNT(*)::bigint AS "cantidadDetalles"
      FROM orden_trabajo 

      INNER JOIN orden_calendario cal 
          ON (orden_trabajo.id_orden = cal.id_orden AND
              cal.fecha = CURRENT_DATE)

      INNER JOIN orden_seguimiento seg 
          ON (orden_trabajo.id_orden = seg.id_orden AND 
              orden_trabajo.id_orden_detalle = seg.id_orden_detalle)

      WHERE seg.estado = 'Normal'
      GROUP BY trabajador
      ORDER BY "cantidadDetalles" DESC
      """, nativeQuery = true)
  List<TrabajadorCargaDTO> getNormalesHoy();

  @Query(value = """
      SELECT 
          seguimiento_por AS "trabajador",
          COUNT(*)::bigint AS "cantidadDetalles"
      FROM orden_seguimiento seg 

      INNER JOIN orden_calendario cal 
          ON (seg.id_orden = cal.id_orden AND
              cal.fecha = CURRENT_DATE)

      WHERE seg.estado = 'Repartida'

      GROUP BY seguimiento_por
      ORDER BY "cantidadDetalles" DESC
      """, nativeQuery = true)
  List<TrabajadorCargaDTO> getRepartidasHoy();

  @Query(value = """
      SELECT COUNT(*) 

      FROM orden_seguimiento seg

      INNER JOIN orden_calendario cal 
          ON (seg.id_orden = cal.id_orden AND
              cal.fecha = CURRENT_DATE)

      WHERE seg.estado = :estado AND
            (:subTipo IS NULL OR seg.sub_tipo = :subTipo)
      """, nativeQuery = true)
  long getCountHoy(@Param("estado") String estado, @Param("subTipo") String subTipo);


}
