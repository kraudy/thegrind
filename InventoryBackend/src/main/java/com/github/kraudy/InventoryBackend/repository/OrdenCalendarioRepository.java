package com.github.kraudy.InventoryBackend.repository;

import com.github.kraudy.InventoryBackend.dto.OrdenCalendarioDTO;
import com.github.kraudy.InventoryBackend.model.OrdenCalendario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrdenCalendarioRepository extends JpaRepository<OrdenCalendario, Long> {

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        DATE(cal.fecha_trabajo) AS fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
        cal.fecha_creacion,
        cal.usuario_creacion,
        cal.fecha_modificacion,
        cal.usuario_modificacion

    FROM orden_calendario cal
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  List<OrdenCalendarioDTO> getAllOrdenCalendario();

  @Query(value ="""
    SELECT
        cal.id_orden,
        cal.fecha_trabajo,
        DATE(cal.fecha_trabajo) AS fecha,
        EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
        EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
        EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
        cal.fecha_creacion,
        cal.usuario_creacion,
        cal.fecha_modificacion,
        cal.usuario_modificacion

    FROM orden_calendario cal
    WHERE cal.id_orden = :idOrden
    ORDER BY cal.id_orden ASC
  """, nativeQuery = true)
  OrdenCalendarioDTO getByIdOrdenCalendario(Long idOrden);

  @Query(value = """
      SELECT
          cal.id_orden,
          cal.fecha_trabajo,
          DATE(cal.fecha_trabajo) AS fecha,
          EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
          EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
          EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
          cal.fecha_creacion,
          cal.usuario_creacion,
          cal.fecha_modificacion,
          cal.usuario_modificacion

      FROM orden_calendario cal
      WHERE DATE(cal.fecha_trabajo) = TO_DATE(:fecha, 'YYYY-MM-DD')
      ORDER BY cal.fecha_trabajo ASC
  """, nativeQuery = true)
  List<OrdenCalendarioDTO> getByFecha(String fecha);

  // Days + counts (uses clean generator)
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

  // All orders in the two-week window
  @Query(value = """
      SELECT
          cal.id_orden,
          cal.fecha_trabajo,
          DATE(cal.fecha_trabajo) AS fecha,
          EXTRACT(DAY FROM cal.fecha_trabajo)::integer AS diaTrabajo,
          EXTRACT(HOUR FROM cal.fecha_trabajo)::integer AS horaTrabajo,
          EXTRACT(MINUTE FROM cal.fecha_trabajo)::integer AS minutoTrabajo,
          cal.fecha_creacion,
          cal.usuario_creacion,
          cal.fecha_modificacion,
          cal.usuario_modificacion
      FROM orden_calendario cal
      WHERE DATE(cal.fecha_trabajo) >= date_trunc('week', CURRENT_DATE)::date 
        AND DATE(cal.fecha_trabajo) <= date_trunc('week', CURRENT_DATE)::date + 13
      ORDER BY cal.fecha_trabajo ASC
      """, nativeQuery = true)
  List<OrdenCalendarioDTO> getOrdersInTwoWeeks();
}
